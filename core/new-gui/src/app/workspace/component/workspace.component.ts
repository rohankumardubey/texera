import { Location } from "@angular/common";
import { AfterViewInit, Component, OnDestroy } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { HttpErrorResponse } from "@angular/common/http";
import { environment } from "../../../environments/environment";
import { Version } from "../../../environments/version";
import { UserService } from "../../common/service/user/user.service";
import { WorkflowPersistService } from "../../common/service/workflow-persist/workflow-persist.service";
import { Workflow } from "../../common/type/workflow";
import { SchemaPropagationService } from "../service/dynamic-schema/schema-propagation/schema-propagation.service";
import { SourceTablesService } from "../service/dynamic-schema/source-tables/source-tables.service";
import { OperatorMetadataService } from "../service/operator-metadata/operator-metadata.service";
import { ResultPanelToggleService } from "../service/result-panel-toggle/result-panel-toggle.service";
import { UndoRedoService } from "../service/undo-redo/undo-redo.service";
import { WorkflowCacheService } from "../service/workflow-cache/workflow-cache.service";
import { WorkflowActionService } from "../service/workflow-graph/model/workflow-action.service";
import { WorkflowWebsocketService } from "../service/workflow-websocket/workflow-websocket.service";
import { NzMessageService } from "ng-zorro-antd/message";
import { WorkflowConsoleService } from "../service/workflow-console/workflow-console.service";
import { debounceTime, distinctUntilChanged, filter, switchMap } from "rxjs/operators";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { OperatorCacheStatusService } from "../service/workflow-status/operator-cache-status.service";
import { of } from "rxjs";
import { isDefined } from "../../common/util/predicate";

@UntilDestroy()
@Component({
  selector: "texera-workspace",
  templateUrl: "./workspace.component.html",
  styleUrls: ["./workspace.component.scss"],
  providers: [
    // uncomment this line for manual testing without opening backend server
    // { provide: OperatorMetadataService, useClass: StubOperatorMetadataService },
  ],
})
export class WorkspaceComponent implements AfterViewInit, OnDestroy {
  public gitCommitHash: string = Version.raw;
  public showResultPanel: boolean = false;
  userSystemEnabled = environment.userSystemEnabled;

  constructor(
    private userService: UserService,
    private resultPanelToggleService: ResultPanelToggleService,
    // list additional services in constructor so they are initialized even if no one use them directly
    private sourceTablesService: SourceTablesService,
    private schemaPropagationService: SchemaPropagationService,
    private undoRedoService: UndoRedoService,
    private operatorCacheStatus: OperatorCacheStatusService,
    private workflowCacheService: WorkflowCacheService,
    private workflowPersistService: WorkflowPersistService,
    private workflowWebsocketService: WorkflowWebsocketService,
    private workflowActionService: WorkflowActionService,
    private workflowConsoleService: WorkflowConsoleService,
    private notificationService: NotificationService,
    private location: Location,
    private route: ActivatedRoute,
    private operatorMetadataService: OperatorMetadataService,
    private message: NzMessageService
  ) {}

  ngAfterViewInit(): void {
    /**
     * On initialization of the workspace, there could be three cases:
     *
     * - with userSystem enabled, usually during prod mode:
     * 1. Accessed by URL `/`, no workflow is in the URL (Cold Start):
     -    - A new `WorkflowActionService.DEFAULT_WORKFLOW` is created, which is an empty workflow with undefined id.
     *    - After an Auto-persist being triggered by a WorkflowAction event, it will create a new workflow in the database
     *    and update the URL with its new ID from database.
     * 2. Accessed by URL `/workflow/:id` (refresh manually, or redirected from dashboard workflow list):
     *    - It will retrieve the workflow from database with the given ID. Because it has an ID, it will be linked to the database
     *    - Auto-persist will be triggered upon all workspace events.
     *
     * - with userSystem disabled, during dev mode:
     * 1. Accessed by URL `/`, with a workflow cached (refresh manually):
     *    - This will trigger the WorkflowCacheService to load the workflow from cache.
     *    - Auto-cache will be triggered upon all workspace events.
     *
     * WorkflowActionService is the single source of the workflow representation. Both WorkflowCacheService and WorkflowPersistService are
     * reflecting changes from WorkflowActionService.
     */
    // clear the current workspace, reset as `WorkflowActionService.DEFAULT_WORKFLOW`
    this.workflowActionService.resetAsNewWorkflow();

    if(this.userSystemEnabled){
      this.registerReEstablishWebsocketUponWIdChange();
    }else{
      let wid = this.route.snapshot.params.id ?? 0;
      this.workflowWebsocketService.openWebsocket(wid);
    }

    this.registerLoadOperatorMetadata();

    this.registerResultPanelToggleHandler();
  }

  ngOnDestroy() {
    this.workflowWebsocketService.closeWebsocket();
  }

  registerResultPanelToggleHandler() {
    this.resultPanelToggleService
      .getToggleChangeStream()
      .pipe(untilDestroyed(this))
      .subscribe(value => (this.showResultPanel = value));
  }

  registerAutoCacheWorkFlow(): void {
    this.workflowActionService
      .workflowChanged()
      .pipe(debounceTime(100))
      .pipe(untilDestroyed(this))
      .subscribe(() => {
        this.workflowCacheService.setCacheWorkflow(this.workflowActionService.getWorkflow());
      });
  }

  registerAutoPersistWorkflow(): void {
    this.workflowActionService
      .workflowChanged()
      .pipe(debounceTime(100))
      .pipe(untilDestroyed(this))
      .subscribe(() => {
        if (this.userService.isLogin()) {
          this.workflowPersistService.createSnapShotCanvas().then(canvas => {
            canvas.toBlob(snapshotBlob => {
              if (snapshotBlob === null) {
                this.notificationService.error("canvas error");
                return;
              }
              this.workflowPersistService
                .persistWorkflow(this.workflowActionService.getWorkflow())
                .pipe(untilDestroyed(this))
                .subscribe((updatedWorkflow: Workflow) => {
                  this.workflowActionService.setWorkflowMetadata(updatedWorkflow);
                  this.workflowPersistService
                    .uploadWorkflowSnapshot(snapshotBlob, updatedWorkflow.wid)
                    .pipe(untilDestroyed(this))
                    .subscribe(
                      () => {
                        this.location.go(`/workflow/${updatedWorkflow.wid}`);
                      },
                      (error: unknown) => {
                        if (error instanceof HttpErrorResponse) {
                          this.notificationService.error(error.error);
                        }
                      }
                    );
                });
            });
          });

          // to sync up with the updated information, such as workflow.wid
        }
      });
  }

  loadWorkflowWithId(wid: number): void {
    // disable the workspace until the workflow is fetched from the backend
    this.workflowActionService.disableWorkflowModification();
    this.workflowPersistService
      .retrieveWorkflow(wid)
      .pipe(untilDestroyed(this))
      .subscribe(
        (workflow: Workflow) => {
          // enable workspace for modification
          this.workflowActionService.enableWorkflowModification();
          // load the fetched workflow
          this.workflowActionService.reloadWorkflow(workflow);
          // clear stack
          this.undoRedoService.clearUndoStack();
          this.undoRedoService.clearRedoStack();
        },
        () => {
          // enable workspace for modification
          this.workflowActionService.enableWorkflowModification();
          // clear the current workflow
          this.workflowActionService.reloadWorkflow(undefined);
          // clear stack
          this.undoRedoService.clearUndoStack();
          this.undoRedoService.clearRedoStack();

          this.message.error("You don't have access to this workflow, please log in with an appropriate account");
        }
      );
  }

  registerLoadOperatorMetadata() {
    this.operatorMetadataService
      .getOperatorMetadata()
      .pipe(filter(metadata => metadata.operators.length !== 0))
      .pipe(untilDestroyed(this))
      .subscribe(() => {
        let wid = this.route.snapshot.params.id;
        if (environment.userSystemEnabled) {
          // load workflow with wid if presented in the URL
          if (wid) {
            // if wid is present in the url, load it from the backend
            this.userService
              .userChanged()
              .pipe(untilDestroyed(this))
              .subscribe(() => this.loadWorkflowWithId(wid));
          } else {
            // no workflow to load, pending to create a new workflow
          }
          // responsible for persisting the workflow to the backend
          this.registerAutoPersistWorkflow();
        } else {
          // load the cached workflow
          this.workflowActionService.reloadWorkflow(this.workflowCacheService.getCachedWorkflow());
          // clear stack
          this.undoRedoService.clearUndoStack();
          this.undoRedoService.clearRedoStack();
          // responsible for saving the existing workflow in cache
          this.registerAutoCacheWorkFlow();
        }
      });
  }

  registerReEstablishWebsocketUponWIdChange() {
    this.workflowActionService
      .workflowMetaDataChanged()
      .pipe(
        switchMap(() => of(this.workflowActionService.getWorkflowMetadata().wid)),
        filter(isDefined),
        distinctUntilChanged()
      )
      .pipe(untilDestroyed(this))
      .subscribe(wid => this.workflowWebsocketService.reopenWebsocket(wid));
  }
}

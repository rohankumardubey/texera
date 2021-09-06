import { Component, Input, OnInit } from "@angular/core";
import { FormBuilder, Validators } from "@angular/forms";
import { NzModalRef } from "ng-zorro-antd/modal";
import { CommentBox, Comment } from "src/app/workspace/types/workflow-common.interface";
import { WorkflowActionService } from "src/app/workspace/service/workflow-graph/model/workflow-action.service";
import { UserService } from "src/app/common/service/user/user.service";


@Component({
    selector: "texera-ngbd-modal-comment-box",
    templateUrl: "./nz-modal-comment-box.component.html",
    styleUrls: ["./nz-modal-comment-box.component.scss"]
})
export class NzModalCommentBoxComponent {
    @Input() commentBox!: CommentBox;

    public commentForm = this.formBuilder.group({
        comment: ["", [Validators.required]]
    });
    constructor(
        public workflowActionService: WorkflowActionService,
        private formBuilder: FormBuilder,
        public userService: UserService,
        public modal: NzModalRef<any, number>
    ) {}

    public onClickAddComment(): void {
        if (this.commentForm.get("comment")?.invalid) {
            alert("Cannot Submit Empty Comment!!");
            return;
        }
        const newComment = this.commentForm.get("comment")?.value;
        this.updateComments(newComment);
        this.commentForm.get("comment")?.setValue("");
    }

    public updateComments(newComment: string): void {
        const currentTime: string = new Date().toISOString();
        const creator = this.userService.getUser()?.name;
        this.workflowActionService.addComment(
          {content: newComment, creationTime: currentTime, creator: creator},
          this.commentBox.commentBoxID
        );
        console.log(this.commentBox);
    }

}


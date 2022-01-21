/*
 * This file is generated by jOOQ.
 */
package edu.uci.ics.texera.web.model.jooq.generated;


import edu.uci.ics.texera.web.model.jooq.generated.tables.File;
import edu.uci.ics.texera.web.model.jooq.generated.tables.KeywordDictionary;
import edu.uci.ics.texera.web.model.jooq.generated.tables.User;
import edu.uci.ics.texera.web.model.jooq.generated.tables.UserConfig;
import edu.uci.ics.texera.web.model.jooq.generated.tables.UserFileAccess;
import edu.uci.ics.texera.web.model.jooq.generated.tables.Workflow;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowOfUser;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowUserAccess;
import edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowVersion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TexeraDb extends SchemaImpl {

    private static final long serialVersionUID = -1924717038;

    /**
     * The reference instance of <code>texera_db</code>
     */
    public static final TexeraDb TEXERA_DB = new TexeraDb();

    /**
     * The table <code>texera_db.file</code>.
     */
    public final File FILE = edu.uci.ics.texera.web.model.jooq.generated.tables.File.FILE;

    /**
     * The table <code>texera_db.keyword_dictionary</code>.
     */
    public final KeywordDictionary KEYWORD_DICTIONARY = edu.uci.ics.texera.web.model.jooq.generated.tables.KeywordDictionary.KEYWORD_DICTIONARY;

    /**
     * The table <code>texera_db.user</code>.
     */
    public final User USER = edu.uci.ics.texera.web.model.jooq.generated.tables.User.USER;

    /**
     * The table <code>texera_db.user_config</code>.
     */
    public final UserConfig USER_CONFIG = edu.uci.ics.texera.web.model.jooq.generated.tables.UserConfig.USER_CONFIG;

    /**
     * The table <code>texera_db.user_file_access</code>.
     */
    public final UserFileAccess USER_FILE_ACCESS = edu.uci.ics.texera.web.model.jooq.generated.tables.UserFileAccess.USER_FILE_ACCESS;

    /**
     * The table <code>texera_db.workflow</code>.
     */
    public final Workflow WORKFLOW = edu.uci.ics.texera.web.model.jooq.generated.tables.Workflow.WORKFLOW;

    /**
     * The table <code>texera_db.workflow_of_user</code>.
     */
    public final WorkflowOfUser WORKFLOW_OF_USER = edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowOfUser.WORKFLOW_OF_USER;

    /**
     * The table <code>texera_db.workflow_user_access</code>.
     */
    public final WorkflowUserAccess WORKFLOW_USER_ACCESS = edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowUserAccess.WORKFLOW_USER_ACCESS;

    /**
     * The table <code>texera_db.workflow_version</code>.
     */
    public final WorkflowVersion WORKFLOW_VERSION = edu.uci.ics.texera.web.model.jooq.generated.tables.WorkflowVersion.WORKFLOW_VERSION;

    /**
     * No further instances allowed
     */
    private TexeraDb() {
        super("texera_db", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        List result = new ArrayList();
        result.addAll(getTables0());
        return result;
    }

    private final List<Table<?>> getTables0() {
        return Arrays.<Table<?>>asList(
            File.FILE,
            KeywordDictionary.KEYWORD_DICTIONARY,
            User.USER,
            UserConfig.USER_CONFIG,
            UserFileAccess.USER_FILE_ACCESS,
            Workflow.WORKFLOW,
            WorkflowOfUser.WORKFLOW_OF_USER,
            WorkflowUserAccess.WORKFLOW_USER_ACCESS,
            WorkflowVersion.WORKFLOW_VERSION);
    }
}

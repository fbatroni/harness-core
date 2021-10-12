/*
 * This file is generated by jOOQ.
 */
package io.harness.timescaledb.tables;

import io.harness.timescaledb.Keys;
import io.harness.timescaledb.Public;
import io.harness.timescaledb.tables.records.PipelineExecutionSummaryCdRecord;

import java.util.Arrays;
import java.util.List;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row21;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class PipelineExecutionSummaryCd extends TableImpl<PipelineExecutionSummaryCdRecord> {
  private static final long serialVersionUID = 1L;

  /**
   * The reference instance of <code>public.pipeline_execution_summary_cd</code>
   */
  public static final PipelineExecutionSummaryCd PIPELINE_EXECUTION_SUMMARY_CD = new PipelineExecutionSummaryCd();

  /**
   * The class holding records for this type
   */
  @Override
  public Class<PipelineExecutionSummaryCdRecord> getRecordType() {
    return PipelineExecutionSummaryCdRecord.class;
  }

  /**
   * The column <code>public.pipeline_execution_summary_cd.id</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, String> ID =
      createField(DSL.name("id"), SQLDataType.CLOB.nullable(false), this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.accountid</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, String> ACCOUNTID =
      createField(DSL.name("accountid"), SQLDataType.CLOB, this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.orgidentifier</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, String> ORGIDENTIFIER =
      createField(DSL.name("orgidentifier"), SQLDataType.CLOB, this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.projectidentifier</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, String> PROJECTIDENTIFIER =
      createField(DSL.name("projectidentifier"), SQLDataType.CLOB, this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.pipelineidentifier</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, String> PIPELINEIDENTIFIER =
      createField(DSL.name("pipelineidentifier"), SQLDataType.CLOB, this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.name</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, String> NAME =
      createField(DSL.name("name"), SQLDataType.CLOB, this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.status</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, String> STATUS =
      createField(DSL.name("status"), SQLDataType.CLOB, this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.moduleinfo_type</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, String> MODULEINFO_TYPE =
      createField(DSL.name("moduleinfo_type"), SQLDataType.CLOB, this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.startts</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, Long> STARTTS =
      createField(DSL.name("startts"), SQLDataType.BIGINT, this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.endts</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, Long> ENDTS =
      createField(DSL.name("endts"), SQLDataType.BIGINT, this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.trigger_type</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, String> TRIGGER_TYPE =
      createField(DSL.name("trigger_type"), SQLDataType.CLOB, this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.author_name</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, String> AUTHOR_NAME =
      createField(DSL.name("author_name"), SQLDataType.CLOB, this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.author_avatar</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, String> AUTHOR_AVATAR =
      createField(DSL.name("author_avatar"), SQLDataType.CLOB, this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.moduleinfo_repository</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, String> MODULEINFO_REPOSITORY =
      createField(DSL.name("moduleinfo_repository"), SQLDataType.CLOB, this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.moduleinfo_branch_name</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, String> MODULEINFO_BRANCH_NAME =
      createField(DSL.name("moduleinfo_branch_name"), SQLDataType.CLOB, this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.source_branch</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, String> SOURCE_BRANCH =
      createField(DSL.name("source_branch"), SQLDataType.CLOB, this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.moduleinfo_event</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, String> MODULEINFO_EVENT =
      createField(DSL.name("moduleinfo_event"), SQLDataType.CLOB, this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.moduleinfo_branch_commit_id</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, String> MODULEINFO_BRANCH_COMMIT_ID =
      createField(DSL.name("moduleinfo_branch_commit_id"), SQLDataType.CLOB, this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.moduleinfo_branch_commit_message</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, String> MODULEINFO_BRANCH_COMMIT_MESSAGE =
      createField(DSL.name("moduleinfo_branch_commit_message"), SQLDataType.CLOB, this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.moduleinfo_author_id</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, String> MODULEINFO_AUTHOR_ID =
      createField(DSL.name("moduleinfo_author_id"), SQLDataType.CLOB, this, "");

  /**
   * The column <code>public.pipeline_execution_summary_cd.planexecutionid</code>.
   */
  public final TableField<PipelineExecutionSummaryCdRecord, String> PLANEXECUTIONID =
      createField(DSL.name("planexecutionid"), SQLDataType.CLOB, this, "");

  private PipelineExecutionSummaryCd(Name alias, Table<PipelineExecutionSummaryCdRecord> aliased) {
    this(alias, aliased, null);
  }

  private PipelineExecutionSummaryCd(
      Name alias, Table<PipelineExecutionSummaryCdRecord> aliased, Field<?>[] parameters) {
    super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
  }

  /**
   * Create an aliased <code>public.pipeline_execution_summary_cd</code> table reference
   */
  public PipelineExecutionSummaryCd(String alias) {
    this(DSL.name(alias), PIPELINE_EXECUTION_SUMMARY_CD);
  }

  /**
   * Create an aliased <code>public.pipeline_execution_summary_cd</code> table reference
   */
  public PipelineExecutionSummaryCd(Name alias) {
    this(alias, PIPELINE_EXECUTION_SUMMARY_CD);
  }

  /**
   * Create a <code>public.pipeline_execution_summary_cd</code> table reference
   */
  public PipelineExecutionSummaryCd() {
    this(DSL.name("pipeline_execution_summary_cd"), null);
  }

  public <O extends Record> PipelineExecutionSummaryCd(
      Table<O> child, ForeignKey<O, PipelineExecutionSummaryCdRecord> key) {
    super(child, key, PIPELINE_EXECUTION_SUMMARY_CD);
  }

  @Override
  public Schema getSchema() {
    return Public.PUBLIC;
  }

  @Override
  public UniqueKey<PipelineExecutionSummaryCdRecord> getPrimaryKey() {
    return Keys.PIPELINE_EXECUTION_SUMMARY_CD_PKEY;
  }

  @Override
  public List<UniqueKey<PipelineExecutionSummaryCdRecord>> getKeys() {
    return Arrays.<UniqueKey<PipelineExecutionSummaryCdRecord>>asList(Keys.PIPELINE_EXECUTION_SUMMARY_CD_PKEY);
  }

  @Override
  public PipelineExecutionSummaryCd as(String alias) {
    return new PipelineExecutionSummaryCd(DSL.name(alias), this);
  }

  @Override
  public PipelineExecutionSummaryCd as(Name alias) {
    return new PipelineExecutionSummaryCd(alias, this);
  }

  /**
   * Rename this table
   */
  @Override
  public PipelineExecutionSummaryCd rename(String name) {
    return new PipelineExecutionSummaryCd(DSL.name(name), null);
  }

  /**
   * Rename this table
   */
  @Override
  public PipelineExecutionSummaryCd rename(Name name) {
    return new PipelineExecutionSummaryCd(name, null);
  }

  // -------------------------------------------------------------------------
  // Row21 type methods
  // -------------------------------------------------------------------------

  @Override
  public Row21<String, String, String, String, String, String, String, String, Long, Long, String, String, String,
      String, String, String, String, String, String, String, String>
  fieldsRow() {
    return (Row21) super.fieldsRow();
  }
}
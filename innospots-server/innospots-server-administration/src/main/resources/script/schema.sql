create table app_definition
(
    app_key      varchar(16)  not null
        primary key,
    created_by   varchar(32)  null,
    created_time datetime(6)  null,
    updated_by   varchar(32)  null,
    updated_time datetime(6)  null,
    project_id   int          null,
    category_id  int          null,
    description  varchar(256) null,
    icon         varchar(32)  null,
    name         varchar(32)  null,
    resources    mediumtext   null,
    settings     text         null,
    status       varchar(32)  null,
    app_path     varchar(32)  null,
    template_key varchar(16)  null
);

create table app_template
(
    template_key varchar(16)  not null
        primary key,
    created_by   varchar(32)  null,
    created_time datetime(6)  null,
    updated_by   varchar(32)  null,
    updated_time datetime(6)  null,
    project_id   int          null,
    category_id  int          null,
    description  varchar(256) null,
    icon         varchar(32)  null,
    name         varchar(32)  null,
    resources    mediumtext   null,
    settings     text         null,
    status       varchar(32)  null,
    author       varchar(64)  null,
    publish_time date         null
);

create table dgm_page
(
    page_id          int auto_increment
        primary key,
    created_by       varchar(32)  null,
    created_time     datetime(6)  null,
    updated_by       varchar(32)  null,
    updated_time     datetime(6)  null,
    project_id       int          null,
    board_ext_config text         null,
    category_id      int          null,
    config           text         null,
    description      varchar(128) null,
    is_delete        bit          null,
    name             varchar(64)  null,
    page_type        varchar(32)  null,
    status           varchar(32)  null,
    sub_name         varchar(64)  null
);

create table dgm_widget
(
    widget_id    int auto_increment
        primary key,
    created_by   varchar(32)   null,
    created_time datetime(6)   null,
    updated_by   varchar(32)   null,
    updated_time datetime(6)   null,
    project_id   int           null,
    config       text          null,
    description  varchar(128)  null,
    is_delete    bit           null,
    name         varchar(128)  null,
    page_id      int           null,
    parent_id    int           null,
    relations    varchar(2048) null,
    type         varchar(16)   null,
    view_codes   varchar(2048) null,
    view_ids     varchar(2048) null,
    widget_key   varchar(64)   null
);

create index idx_page_wdt
    on dgm_widget (page_id);

create table dgm_workspace
(
    workspace_id int auto_increment
        primary key,
    created_by   varchar(32) null,
    created_time datetime(6) null,
    updated_by   varchar(32) null,
    updated_time datetime(6) null,
    project_id   int         null,
    page_id      int         null,
    user_id      int         null
);

create table ds_credential_info
(
    credential_key       varchar(8)    not null
        primary key,
    created_by           varchar(32)   null,
    created_time         datetime(6)   null,
    updated_by           varchar(32)   null,
    updated_time         datetime(6)   null,
    project_id           int           null,
    credential_type_code varchar(32)   null,
    encrypt_form_values  varchar(2048) null,
    name                 varchar(32)   null,
    props                varchar(2048) null
);

create index credential_type
    on ds_credential_info (credential_type_code);

create table ds_credential_type
(
    type_code      varchar(32)   not null
        primary key,
    created_by     varchar(32)   null,
    created_time   datetime(6)   null,
    updated_by     varchar(32)   null,
    updated_time   datetime(6)   null,
    project_id     int           null,
    auth_option    varchar(32)   null,
    connector_name varchar(32)   null,
    form_values    varchar(2048) null,
    icon           varchar(128)  null,
    name           varchar(32)   null,
    props          varchar(2048) null
);

create index connector_name
    on ds_credential_type (connector_name);

create table ds_schema_field
(
    field_id      int auto_increment
        primary key,
    created_by    varchar(32)   null,
    created_time  datetime(6)   null,
    updated_by    varchar(32)   null,
    updated_time  datetime(6)   null,
    project_id    int           null,
    code          varchar(64)   null,
    comment       varchar(128)  null,
    config        varchar(2048) null,
    default_value varchar(32)   null,
    field_scope   varchar(16)   null,
    name          varchar(64)   null,
    pkey          bit           null,
    registry_code varchar(64)   null,
    registry_id   varchar(16)   null,
    value_type    varchar(32)   null
);

create index idx_registry_fld
    on ds_schema_field (registry_id);

create table ds_schema_registry
(
    registry_id    varchar(16)  not null
        primary key,
    created_by     varchar(32)  null,
    created_time   datetime(6)  null,
    updated_by     varchar(32)  null,
    updated_time   datetime(6)  null,
    project_id     int          null,
    app_key        varchar(16)  null,
    category_id    int          null,
    code           varchar(64)  null,
    configs        text         null,
    credential_key varchar(8)   null,
    description    varchar(256) null,
    name           varchar(64)  null,
    registry_type  varchar(16)  null,
    scope          varchar(16)  null,
    script         text         null
);

create index idx_app_key
    on ds_schema_registry (app_key);

create index idx_credential_key
    on ds_schema_registry (credential_key);

create table ext_definition
(
    ext_definition_id int auto_increment
        primary key,
    created_by        varchar(32)  null,
    created_time      datetime(6)  null,
    updated_by        varchar(32)  null,
    updated_time      datetime(6)  null,
    author            varchar(32)  null,
    description       varchar(256) null,
    ext_icon          varchar(128) null,
    ext_key           varchar(64)  null,
    ext_name          varchar(128) null,
    ext_version       varchar(16)  null,
    extension_source  varchar(16)  null,
    extension_status  varchar(16)  null,
    kernel_version    varchar(16)  null,
    last_app_version  varchar(16)  null,
    last_description  varchar(256) null,
    last_publish_time date         null,
    publish_time      date         null,
    signature         varchar(64)  null,
    vendor            varchar(32)  null,
    zip_file_names    varchar(512) null
);

create table ext_installment
(
    ext_installment_id int auto_increment
        primary key,
    created_by         varchar(32)  null,
    created_time       datetime(6)  null,
    updated_by         varchar(32)  null,
    updated_time       datetime(6)  null,
    project_id         int          null,
    ext_key            varchar(64)  null,
    extension_status   varchar(16)  null,
    install_person     varchar(32)  null,
    install_time       datetime(6)  null,
    install_version    varchar(16)  null,
    license_key        varchar(256) null,
    license_time       varchar(32)  null
);

create table flow_execution
(
    flow_execution_id        varchar(64)   not null
        primary key,
    created_by               varchar(32)   null,
    created_time             datetime(6)   null,
    updated_by               varchar(32)   null,
    updated_time             datetime(6)   null,
    project_id               int           null,
    channel                  varchar(16)   null,
    current_node_keys        varchar(64)   null,
    data_time                varchar(32)   null,
    end_time                 varchar(32)   null,
    exec_mode                varchar(16)   null,
    execution_uri            varchar(128)  null,
    flow_instance_id         bigint        null,
    hit_node_number          int           null,
    location                 varchar(32)   null,
    message                  varchar(2048) null,
    parent_flow_execution_id varchar(64)   null,
    result                   varchar(2048) null,
    result_code              varchar(8)    null,
    revision                 int           null,
    skip_flow_execution      bit           null,
    skip_node_execution      bit           null,
    source                   varchar(128)  null,
    start_time               varchar(32)   null,
    status                   varchar(16)   null,
    uuid                     varchar(32)   null,
    uuid_type                varchar(8)    null
);

create index idx_flow_rev
    on flow_execution (flow_instance_id, revision);

create table flow_execution_context
(
    execution_id     varchar(64) not null
        primary key,
    context_type     varchar(8)  null,
    created_time     datetime(6) null,
    flow_instance_id bigint      null,
    input_size       int         null,
    inputs           longtext    null,
    node_paths       text        null,
    output_size      int         null,
    outputs          longtext    null
);

create table flow_execution_node
(
    node_execution_id   varchar(64)  not null
        primary key,
    created_by          varchar(32)  null,
    created_time        datetime(6)  null,
    updated_by          varchar(32)  null,
    updated_time        datetime(6)  null,
    project_id          int          null,
    end_time            varchar(32)  null,
    exec_mode           varchar(16)  null,
    flow_execution_id   varchar(64)  null,
    flow_instance_id    bigint       null,
    flow_start_time     varchar(32)  null,
    message             text         null,
    next                bit          null,
    next_node_keys      varchar(256) null,
    node_code           varchar(32)  null,
    node_key            varchar(16)  null,
    node_name           varchar(32)  null,
    previous_node_keys  varchar(256) null,
    revision            int          null,
    sequence_number     int          null,
    skip_node_execution bit          null,
    start_time          varchar(32)  null,
    status              varchar(16)  null
);

create index idx_flow_ext_node
    on flow_execution_node (flow_execution_id);

create index idx_flow_rev_ext_node
    on flow_execution_node (flow_instance_id, revision);

create table flow_execution_node_scheduled
(
    node_execution_id varchar(64)   not null
        primary key,
    created_by        varchar(32)   null,
    created_time      datetime(6)   null,
    updated_by        varchar(32)   null,
    updated_time      datetime(6)   null,
    project_id        int           null,
    flow_execution_id varchar(64)   null,
    flow_instance_id  bigint        null,
    message           varchar(1024) null,
    node_key          varchar(16)   null,
    scheduled_time    datetime(6)   null,
    server_key        varchar(64)   null,
    sharding_key      int           null,
    status            varchar(16)   null
);

create index idx_server_sharding
    on flow_execution_node_scheduled (scheduled_time, status, server_key, sharding_key);

create table flow_instance
(
    workflow_instance_id bigint auto_increment
        primary key,
    created_by           varchar(32)  null,
    created_time         datetime(6)  null,
    updated_by           varchar(32)  null,
    updated_time         datetime(6)  null,
    project_id           int          null,
    category_id          int          null,
    datasource_code      varchar(8)   null,
    description          varchar(128) null,
    end_date             date         null,
    flow_key             varchar(32)  null,
    name                 varchar(32)  null,
    online_time          datetime(6)  null,
    page_id              int          null,
    revision             int          null,
    start_date           date         null,
    status               varchar(16)  null,
    table_name           varchar(32)  null,
    template_code        varchar(16)  null,
    trigger_code         varchar(32)  null
);

create index idx_status_revision
    on flow_instance (status, updated_time, revision);

create table flow_instance_cache
(
    workflow_instance_id bigint auto_increment
        primary key,
    created_by           varchar(32) null,
    created_time         datetime(6) null,
    updated_by           varchar(32) null,
    updated_time         datetime(6) null,
    project_id           int         null,
    flow_instance        mediumtext  null
);

create table flow_instance_edge
(
    edge_id              bigint auto_increment
        primary key,
    created_by           varchar(32)  null,
    created_time         datetime(6)  null,
    updated_by           varchar(32)  null,
    updated_time         datetime(6)  null,
    project_id           int          null,
    data                 text         null,
    end_point            varchar(128) null,
    name                 varchar(32)  null,
    revision             int          null,
    source               varchar(64)  null,
    source_anchor        varchar(64)  null,
    start_point          varchar(128) null,
    status               varchar(32)  null,
    style                varchar(256) null,
    target               varchar(64)  null,
    target_anchor        varchar(64)  null,
    type                 varchar(32)  null,
    workflow_instance_id bigint       null
);

create index idx_flow_rev_edge
    on flow_instance_edge (workflow_instance_id, revision);

create table flow_instance_node
(
    node_instance_id      bigint auto_increment
        primary key,
    created_by            varchar(32)   null,
    created_time          datetime(6)   null,
    updated_by            varchar(32)   null,
    updated_time          datetime(6)   null,
    project_id            int           null,
    action                text          null,
    action_script_type    varchar(16)   null,
    continue_on_fail      bit           null,
    data                  text          null,
    description           varchar(256)  null,
    display_name          varchar(32)   null,
    failure_branch        bit           null,
    height                int           null,
    input_fields          varchar(1024) null,
    max_tries             int           null,
    methods               text          null,
    name                  varchar(32)   null,
    node_definition_id    int           null,
    node_key              varchar(64)   null,
    node_type             varchar(128)  null,
    output_fields         text          null,
    pause_flag            bit           null,
    ports                 varchar(1024) null,
    retry_on_fail         bit           null,
    retry_wait_time_mills int           null,
    revision              int           null,
    status                varchar(16)   null,
    width                 int           null,
    workflow_instance_id  bigint        null,
    x                     int           null,
    y                     int           null
);

create index idx_flow_rev_node
    on flow_instance_node (workflow_instance_id, revision);

create table flow_instance_revision
(
    flow_revision_id     bigint auto_increment
        primary key,
    created_by           varchar(32)  null,
    created_time         datetime(6)  null,
    updated_by           varchar(32)  null,
    updated_time         datetime(6)  null,
    project_id           int          null,
    description          varchar(128) null,
    node_number          bigint       null,
    revision             int          null,
    workflow_instance_id bigint       null
);

create table flow_node_definition
(
    node_id              int auto_increment
        primary key,
    created_by           varchar(32)  null,
    created_time         datetime(6)  null,
    updated_by           varchar(32)  null,
    updated_time         datetime(6)  null,
    project_id           int          null,
    code                 varchar(32)  null,
    credential_type_code varchar(32)  null,
    deletable            bit          null,
    description          varchar(256) null,
    flow_code            varchar(32)  null,
    icon                 varchar(64)  null,
    name                 varchar(32)  null,
    node_type            varchar(128) null,
    primitive            varchar(16)  null,
    resources            mediumtext   null,
    scripts              mediumtext   null,
    settings             text         null,
    status               varchar(16)  null,
    used                 bit          null,
    vendor               varchar(32)  null
);

create index idx_flow_code
    on flow_node_definition (flow_code);

create index idx_node_code
    on flow_node_definition (code);

create table flow_node_group
(
    node_group_id int auto_increment
        primary key,
    created_by    varchar(32) null,
    created_time  datetime(6) null,
    updated_by    varchar(32) null,
    updated_time  datetime(6) null,
    project_id    int         null,
    code          varchar(64) null,
    flow_tpl_id   int         null,
    name          varchar(64) null,
    position      int         null,
    scopes        varchar(16) null
);

create index idx_flow_tpl_group
    on flow_node_group (flow_tpl_id);

create table flow_node_group_node
(
    node_group_node_id int auto_increment
        primary key,
    created_by         varchar(32) null,
    created_time       datetime(6) null,
    updated_by         varchar(32) null,
    updated_time       datetime(6) null,
    project_id         int         null,
    flow_tpl_id        int         null,
    node_group_id      int         null,
    node_id            int         null
);

create index idx_flow_tpl_node
    on flow_node_group_node (flow_tpl_id);

create table flow_template
(
    flow_tpl_id  int auto_increment
        primary key,
    created_by   varchar(32)  null,
    created_time datetime(6)  null,
    updated_by   varchar(32)  null,
    updated_time datetime(6)  null,
    project_id   int          null,
    description  varchar(128) null,
    status       varchar(16)  null,
    tpl_code     varchar(32)  null,
    tpl_name     varchar(32)  null,
    type         varchar(16)  null
);

create index idx_tpl_code
    on flow_template (tpl_code);

create table i18n_currency
(
    currency_id    int auto_increment
        primary key,
    created_by     varchar(32) null,
    created_time   datetime(6) null,
    updated_by     varchar(32) null,
    updated_time   datetime(6) null,
    code           varchar(16) null,
    decimal_digits int         null,
    left_sign      varchar(4)  null,
    name           varchar(16) null,
    right_sign     varchar(4)  null,
    status         varchar(16) null
);

create table i18n_dictionary
(
    dictionary_id int auto_increment
        primary key,
    created_by    varchar(32)  null,
    created_time  datetime(6)  null,
    updated_by    varchar(32)  null,
    updated_time  datetime(6)  null,
    app           varchar(32)  null,
    code          varchar(64)  null,
    description   varchar(128) null,
    module        varchar(32)  null
);

create index idx_dict_code
    on i18n_dictionary (code);

create table i18n_language
(
    language_id        int auto_increment
        primary key,
    created_by         varchar(32) null,
    created_time       datetime(6) null,
    updated_by         varchar(32) null,
    updated_time       datetime(6) null,
    currency_id        int         null,
    date_format        varchar(16) null,
    decimal_separator  varchar(4)  null,
    default_lan        bit         null,
    icon               varchar(16) null,
    locale             varchar(16) null,
    name               varchar(32) null,
    status             varchar(16) null,
    thousand_separator varchar(4)  null,
    time_format        varchar(16) null
);

create table i18n_trans_message
(
    message_id    int auto_increment
        primary key,
    created_by    varchar(32)  null,
    created_time  datetime(6)  null,
    updated_by    varchar(32)  null,
    updated_time  datetime(6)  null,
    dictionary_id int          null,
    locale        varchar(16)  null,
    message       varchar(512) null
);

create table schedule_job_execution
(
    execution_id        varchar(32)  not null
        primary key,
    created_by          varchar(32)  null,
    created_time        datetime(6)  null,
    updated_by          varchar(32)  null,
    updated_time        datetime(6)  null,
    project_id          int          null,
    context             text         null,
    detail_uri          varchar(128) null,
    end_time            datetime(6)  null,
    execution_status    varchar(16)  null,
    ext_execution_id    varchar(32)  null,
    fail_count          bigint       null,
    instance_key        varchar(64)  null,
    job_class           varchar(64)  null,
    job_key             varchar(16)  null,
    job_name            varchar(32)  null,
    job_type            varchar(16)  null,
    key_type            varchar(16)  null,
    message             text         null,
    origin_execution_id varchar(32)  null,
    output              mediumtext   null,
    parent_execution_id varchar(32)  null,
    percent             int          null,
    resource_key        varchar(32)  null,
    scopes              varchar(32)  null,
    self_end_time       datetime(6)  null,
    sequence_number     int          null,
    server_key          varchar(32)  null,
    start_time          datetime(6)  null,
    sub_job_count       bigint       null,
    success_count       bigint       null,
    version             int          null
);

create table schedule_job_info
(
    job_key       varchar(16)   not null
        primary key,
    created_by    varchar(32)   null,
    created_time  datetime(6)   null,
    updated_by    varchar(32)   null,
    updated_time  datetime(6)   null,
    project_id    int           null,
    category_id   int           null,
    end_time      datetime(6)   null,
    job_class     varchar(128)  null,
    job_name      varchar(32)   null,
    job_status    varchar(16)   null,
    job_type      varchar(16)   null,
    params        text          null,
    resource_key  varchar(32)   null,
    schedule_mode varchar(16)   null,
    scopes        varchar(32)   null,
    splitter      varchar(128)  null,
    start_time    datetime(6)   null,
    sub_job_count int           null,
    time_config   varchar(2056) null
);

create table schedule_ready_queue
(
    job_ready_key       varchar(32) not null
        primary key,
    created_by          varchar(32) null,
    created_time        datetime(6) null,
    updated_by          varchar(32) null,
    updated_time        datetime(6) null,
    project_id          int         null,
    context             text        null,
    ext_execution_id    varchar(32) null,
    group_key           varchar(8)  null,
    instance_key        varchar(64) null,
    job_class           varchar(64) null,
    job_key             varchar(16) null,
    job_name            varchar(32) null,
    job_type            varchar(16) null,
    key_type            varchar(16) null,
    message_status      varchar(16) null,
    origin_execution_id varchar(32) null,
    parent_execution_id varchar(32) null,
    resource_key        varchar(32) null,
    scopes              varchar(32) null,
    sequence_number     int         null,
    server_key          varchar(32) null,
    sub_job_count       int         null,
    version             int         null
);

create table sys_avatar_resource
(
    avatar_id    int auto_increment
        primary key,
    created_by   varchar(32) null,
    created_time datetime(6) null,
    updated_by   varchar(32) null,
    updated_time datetime(6) null,
    image_base64 mediumtext  null,
    image_sort   int         null,
    image_type   varchar(16) null,
    resource_id  varchar(32) null
);

create table sys_category
(
    category_id        int auto_increment
        primary key,
    created_by         varchar(32)  null,
    created_time       datetime(6)  null,
    updated_by         varchar(32)  null,
    updated_time       datetime(6)  null,
    project_id         int          null,
    category_code      varchar(128) null,
    category_name      varchar(128) null,
    category_type      varchar(16)  null,
    orders             int          null,
    parent_category_id int          null
);

create table sys_config
(
    config_id     int auto_increment
        primary key,
    created_by    varchar(32)   null,
    created_time  datetime(6)   null,
    updated_by    varchar(32)   null,
    updated_time  datetime(6)   null,
    project_id    int           null,
    config_code   varchar(32)   null,
    config_group  varchar(8)    null,
    config_name   varchar(32)   null,
    config_value  varchar(1024) null,
    encrypt_type  varchar(8)    null,
    place_holder  varchar(32)   null,
    resource_id   int           null,
    resource_type varchar(16)   null,
    tips          varchar(32)   null,
    value_type    varchar(16)   null
);

create table sys_function
(
    function_id   int           not null
        primary key,
    cate_name     varchar(16)   null,
    cate_type     varchar(16)   null,
    description   varchar(128)  null,
    expression    varchar(2048) null,
    function_type varchar(16)   null,
    name          varchar(64)   null,
    param_types   varchar(128)  null,
    return_type   varchar(16)   null
);

create table sys_login_log
(
    log_id         bigint auto_increment
        primary key,
    created_by     varchar(32)  null,
    created_time   datetime(6)  null,
    updated_by     varchar(32)  null,
    updated_time   datetime(6)  null,
    browser        varchar(32)  null,
    city           varchar(32)  null,
    detail         varchar(128) null,
    ip             varchar(32)  null,
    login_time     datetime(6)  null,
    os             varchar(32)  null,
    province       varchar(32)  null,
    status         varchar(16)  null,
    user_avatar    varchar(64)  null,
    user_id        int          null,
    user_name      varchar(32)  null,
    user_role_name varchar(128) null
);

create table sys_menu_resource
(
    resource_id      int auto_increment
        primary key,
    created_by       varchar(32)  null,
    created_time     datetime(6)  null,
    updated_by       varchar(32)  null,
    updated_time     datetime(6)  null,
    project_id       int          null,
    app_key          varchar(64)  null,
    app_name         varchar(128) null,
    i18n_names       text         null,
    icon             varchar(32)  null,
    item_key         varchar(64)  null,
    item_type        varchar(16)  null,
    load_mode        varchar(16)  null,
    menu_group       varchar(16)  null,
    method           varchar(32)  null,
    name             varchar(64)  null,
    open_mode        varchar(16)  null,
    orders           int          null,
    parent_id        int          null,
    parent_item_keys varchar(128) null,
    show_menu        bit          null,
    status           bit          null,
    uri              varchar(128) null
);

create table sys_notification_channel
(
    channel_id    int auto_increment
        primary key,
    created_by    varchar(32)  null,
    created_time  datetime(6)  null,
    updated_by    varchar(32)  null,
    updated_time  datetime(6)  null,
    project_id    int          null,
    channel_name  varchar(16)  null,
    channel_type  varchar(16)  null,
    credential_id int          null,
    params        varchar(256) null,
    registry_id   varchar(16)  null,
    status        varchar(16)  null
);

create table sys_notification_message
(
    message_id      bigint auto_increment
        primary key,
    created_by      varchar(32)   null,
    created_time    datetime(6)   null,
    updated_by      varchar(32)   null,
    updated_time    datetime(6)   null,
    project_id      int           null,
    channel_id      int           null,
    channel_name    varchar(16)   null,
    event_code      varchar(16)   null,
    event_name      varchar(64)   null,
    ext_name        varchar(32)   null,
    message         varchar(2048) null,
    module          varchar(32)   null,
    read_flag       bit           null,
    read_time       datetime(6)   null,
    receive_user    varchar(32)   null,
    receive_user_id int           null,
    record_time     datetime(6)   null,
    send_result     varchar(128)  null,
    send_success    bit           null,
    title           varchar(64)   null
);

create table sys_notification_setting
(
    setting_id   int auto_increment
        primary key,
    created_by   varchar(32)  null,
    created_time datetime(6)  null,
    updated_by   varchar(32)  null,
    updated_time datetime(6)  null,
    project_id   int          null,
    channels     varchar(128) null,
    event_code   varchar(16)  null,
    event_name   varchar(64)  null,
    ext_key      varchar(32)  null,
    module_key   varchar(32)  null,
    role_groups  varchar(128) null
);

create table sys_operation_log
(
    log_id        bigint auto_increment
        primary key,
    created_by    varchar(32)  null,
    created_time  datetime(6)  null,
    updated_by    varchar(32)  null,
    updated_time  datetime(6)  null,
    project_id    int          null,
    browser       varchar(64)  null,
    city          varchar(32)  null,
    detail        varchar(256) null,
    device        varchar(32)  null,
    ip            varchar(32)  null,
    language      varchar(128) null,
    location      varchar(32)  null,
    manufacturer  varchar(32)  null,
    module        varchar(64)  null,
    operate_time  datetime(6)  null,
    operate_type  varchar(16)  null,
    os_system     varchar(32)  null,
    province      varchar(32)  null,
    request_path  varchar(128) null,
    resolution    varchar(64)  null,
    resource_id   varchar(64)  null,
    resource_name varchar(64)  null,
    resource_type varchar(32)  null,
    roles         varchar(128) null,
    user_avatar   varchar(64)  null,
    user_id       int          null,
    username      varchar(32)  null
);

create table sys_role
(
    role_id      int auto_increment
        primary key,
    created_by   varchar(32) null,
    created_time datetime(6) null,
    updated_by   varchar(32) null,
    updated_time datetime(6) null,
    project_id   int         null,
    admin        bit         null,
    role_code    varchar(32) null,
    role_name    varchar(32) null
);

create table sys_role_resource
(
    role_resource_id int auto_increment
        primary key,
    created_by       varchar(32) null,
    created_time     datetime(6) null,
    updated_by       varchar(32) null,
    updated_time     datetime(6) null,
    project_id       int         null,
    item_key         varchar(64) null,
    resource_type    varchar(16) null,
    role_id          int         null
);

create table sys_service_registry
(
    server_id      bigint       not null
        primary key,
    created_by     varchar(32)  null,
    created_time   datetime(6)  null,
    domain_ip      varchar(16)  null,
    group_name     varchar(16)  null,
    port           int          null,
    service_name   varchar(128) null,
    service_role   varchar(16)  not null,
    service_status varchar(16)  not null,
    service_type   varchar(32)  not null,
    tags           varchar(32)  null,
    update_by      varchar(32)  null,
    updated_time   datetime(6)  null,
    constraint idx_ip_port
        unique (domain_ip, port)
);

create table sys_task_execution
(
    task_execution_id varchar(64)  not null
        primary key,
    created_by        varchar(32)  null,
    created_time      datetime(6)  null,
    updated_by        varchar(32)  null,
    updated_time      datetime(6)  null,
    app_key           varchar(32)  null,
    app_name          varchar(32)  null,
    detail_url        varchar(255) null,
    end_time          datetime(6)  null,
    execution_status  varchar(16)  null,
    extension_key     varchar(64)  null,
    extension_type    varchar(16)  null,
    message           text         null,
    param_context     text         null,
    percent           int          null,
    start_time        datetime(6)  null,
    task_name         varchar(64)  null
);

create table sys_temp_cache
(
    cache_key    varchar(32) not null
        primary key,
    created_by   varchar(32) null,
    created_time datetime(6) null,
    updated_by   varchar(32) null,
    updated_time datetime(6) null,
    project_id   int         null,
    cache_value  mediumtext  null,
    expire_time  datetime(6) null
);

create table sys_todo_task
(
    task_id           int auto_increment
        primary key,
    created_by        varchar(32)  null,
    created_time      datetime(6)  null,
    updated_by        varchar(32)  null,
    updated_time      datetime(6)  null,
    project_id        int          null,
    description       text         null,
    end_date          date         null,
    principal_user_id int          null,
    start_date        date         null,
    task_name         varchar(128) null,
    task_priority     varchar(16)  null,
    task_status       varchar(16)  null
);

create table sys_todo_task_comment
(
    comment_id   int auto_increment
        primary key,
    created_by   varchar(32) null,
    created_time datetime(6) null,
    updated_by   varchar(32) null,
    updated_time datetime(6) null,
    project_id   int         null,
    content      text        null,
    task_id      int         null
);

create table sys_todo_task_tag
(
    tag_id       int auto_increment
        primary key,
    created_by   varchar(32) null,
    created_time datetime(6) null,
    updated_by   varchar(32) null,
    updated_time datetime(6) null,
    project_id   int         null,
    tag_name     varchar(32) null,
    task_id      int         null
);

create table sys_user
(
    user_id          int auto_increment
        primary key,
    created_by       varchar(32)  null,
    created_time     datetime(6)  null,
    updated_by       varchar(32)  null,
    updated_time     datetime(6)  null,
    avatar_key       varchar(64)  null,
    department       varchar(128) null,
    email            varchar(128) null,
    last_access_time datetime(6)  null,
    last_org_id      int          null,
    last_project_id  int          null,
    login_times      int          null,
    mobile           varchar(32)  null,
    on_off           varchar(16)  null,
    password         varchar(128) null,
    real_name        varchar(128) null,
    remark           varchar(255) null,
    status           varchar(16)  null,
    user_name        varchar(128) null
);

create table sys_user_role
(
    user_role_id int auto_increment
        primary key,
    created_by   varchar(32) null,
    created_time datetime(6) null,
    updated_by   varchar(32) null,
    updated_time datetime(6) null,
    project_id   int         null,
    role_id      int         null,
    user_id      int         null
);



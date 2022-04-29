CREATE TABLE T_PARAMETRO  (
	n_parametro     integer not null,
	x_codigo        varchar(50),
	x_valor         varchar(50),
	x_descripcion   varchar(200),
	primary key (n_parametro)
);

INSERT INTO T_PARAMETRO(n_parametro, x_codigo, x_valor, x_descripcion)
VALUES (100, 'VERSION_APP', '1.0.0', 'Version del sistema movil');

INSERT INTO T_PARAMETRO(n_parametro, x_codigo, x_valor, x_descripcion)
VALUES (101, 'ULTIMA_FECHA_SYNC_MAESTROS', 'null', 'Ultima Sincronizacion de Maestros');

INSERT INTO T_PARAMETRO(n_parametro, x_codigo, x_valor, x_descripcion)
VALUES (102, 'ULTIMA_FECHA_SYNC_INFORMACION', 'null', 'Ultima Sincronizacion de Información');

INSERT INTO T_PARAMETRO(n_parametro, x_codigo, x_valor, x_descripcion)
VALUES (103, 'ULTIMA_FECHA_SYNC_LISTA_VERIFICACION', 'null', 'Ultima Sincronizacion Lista Verificacion');

CREATE TABLE T_SC_USER (
	usuario_id              integer primary key autoincrement not null,
	sc_user_id              numeric(10,0) null,
	empresa                 varchar(200) null,
	pais                    varchar(200) null,
	user_login              varchar(200) null,
	password                varchar(100) null,
	usuario                 varchar(200) null,
	dni                     varchar(8) null,
	fb_empleado_id          numeric(10,0) null,
	nombre_empleado         varchar(200) null,
	fb_uea_pe_id            numeric(10,0) null,
	fb_uea_pe_abr           varchar(50) null,
	ip_o_dominio_servidor   varchar(200) null,
	user_login_servidor     varchar(200) null,
    password_servidor       varchar(100) null,
    url_ext                 varchar(100) null,
    url_app                 varchar(100) null,
    arroba                  varchar(100) null,
    enterprise              varchar(100) null
);

------------------FB---------------------


CREATE TABLE FB_UEA_PE (
    fb_uea_pe_id    numeric(10,0) not null,
    codigo          varchar(50) null,
    nombre          varchar(100) null,
    sc_user_id      numeric(10,0) null,
    primary key (fb_uea_pe_id)
);

CREATE TABLE FB_GERENCIA (
    fb_gerencia_id    numeric(10,0) primary key not null,
    fb_uea_pe_id    numeric(10,0) not null,
    codigo          varchar(50) null,
    nombre          varchar(100) null
);

CREATE TABLE FB_EMPRESA_ESPECIALIZADA(
    fb_empresa_especializada_id     numeric(10,0) not null,
    razon_social                    varchar(500) null,
    ruc_empresa                     varchar(20) null,
    g_rol_empresa_id                numeric(10,0) null,
    primary key (fb_empresa_especializada_id)
);

CREATE TABLE FB_AREA (
    fb_area_id  numeric(10,0) primary key not null,
    fb_gerencia_id  numeric(10,0) not null,
    codigo      varchar(50) null,
    nombre      varchar(100) null
);

---------------------------------

CREATE TABLE OPS_TIPO_RESULTADO (
    ops_tipo_resultado_id   numeric(10,0) not null,
    codigo                  varchar(50) null,
    nombre                  varchar(200) null,
    primary key (ops_tipo_resultado_id)
);

CREATE TABLE OPS_LISTA_VERIFICACION (
    ops_lista_verificacion_id   numeric(10,0) not null,
    codigo                      varchar(50) null,
    nombre                      varchar(200) null,
    ops_tipo_resultado_id       numeric(10,0) null,
    primary key (ops_lista_verificacion_id)
);

CREATE TABLE OPS_LISTA_VERIF_CATEGORIA(
    ops_lista_verif_categoria_id    numeric(10,0) not null,
    ops_lista_verificacion_id       numeric(10,0) null,
    nombre                          varchar(100) null,
    primary key (ops_lista_verif_categoria_id)
);

CREATE TABLE OPS_LISTA_VERIF_SECCION (
    ops_lista_verif_seccion_id      numeric(10,0) not null,
    ops_lista_verif_categoria_id    numeric(10,0) null,
    ops_lista_verificacion_id       numeric(10,0) null,
    nombre                          varchar(100) null,
    orden                           varchar(20) null,
    primary key (ops_lista_verif_seccion_id)
);

CREATE TABLE OPS_LISTA_VERIF_PREGUNTA(
    ops_lista_verif_pregunta_id     numeric(10,0) not null,
    ops_lista_verif_seccion_id      numeric(10,0) null,
    ops_lista_verif_categoria_id    numeric(10,0) null,
    ops_lista_verificacion_id       numeric(10,0) null,
    nombre                          varchar(800) null,
    flag_pregunta                   numeric(1,0) null,
    orden                           varchar(20) null,
    primary key (ops_lista_verif_pregunta_id)
);

CREATE TABLE OPS_LISTA_VERIF_RESULTADO(
    ops_lista_verif_resultado_id    numeric(10,0) not null,
    ops_tipo_resultado_id           numeric(10,0) null,
    codigo                          varchar(50) null,
    nombre                          varchar(100) null,
    primary key (ops_lista_verif_resultado_id)
);



CREATE TABLE T_TURNO (
    code        varchar(500) primary key not null,
    name        varchar(500) null
);

CREATE TABLE OPS_REGISTRO_GENERALES(
    ops_registro_generales_id           integer primary key autoincrement not null,
    fb_uea_pe_id                        numeric(10,0) null,
    codigo                              varchar(50) null,
    g_tipo_origen_id                    numeric(10,0) null,
    fecha_ops                           varchar(10) null,
    hora_ops                            varchar(10) null,
    turno                               varchar(50) null,
    fb_area_id                          numeric(10,0) null,
    g_rol_empresa_id                    numeric(10,0) null,
    fb_empresa_especializada_id         numeric(10,0) null,
    fb_empleado_id                      numeric(10,0) null,
    ops_lista_verificacion_id           numeric(10,0) null,
    ops_tipo_resultado_id               numeric(10,0) null,
    latitud                             numeric(10,8) null,
    longitud                            numeric(10,8) null,
    fb_area_nombre                      varchar(100) null,
    turno_nombre                        varchar(500) null,
    fb_empresa_especializada_nombre     varchar(500) null,
    fb_empleado_nombre_completo         varchar(200) null,
    estado                              numeric(10,0) null,
    id_generado_syncronizacion          varchar(50) null,
    flag                                varchar(1) null
);

CREATE TABLE OPS_REGISTRO_RESULTADO(
    ops_registro_resultado_id       integer primary key autoincrement not null,
    ops_registro_generales_id       numeric(10,0) null,
    ops_lista_verif_pregunta_id     numeric(10,0) null,
    ops_lista_verif_seccion_id      numeric(10,0) null,
    ops_lista_verif_categoria_id    numeric(10,0) null,
    ops_lista_verif_resultado_id    numeric(10,0) null,
    observacion                     varchar(400) null,
    ruta                            varchar(400) null,
    nombre_imagen                   varchar(50) null,
    id_generado_syncronizacion      varchar(50) null
);

CREATE TABLE T_SECCION_COMPLETA(
    seccion_completa_id             integer primary key autoincrement not null,
    ops_registro_generales_id       numeric(10,0) null,
    ops_lista_verif_seccion_id      numeric(10,0) null,
    ops_lista_verif_categoria_id    numeric(10,0) null,
    flagCompleto                    numeric(1,0) null
);


CREATE TABLE SAC_ACCION_CORRECTIVA (
	sac_accion_correctiva_id    numeric (10,0) primary key not null,
	codigo_accion_correctiva    varchar(200) null,
	accion_correctiva_detalle varchar(200) null,
	fecha_acordada_ejecucion    varchar(200) null,
	nombre_responsable_correccion varchar(200) null,
	origen           varchar(200) null,
	uea_id numeric(10,0) null,
    fecha_ejecucion    varchar(200) null,
    evidencia_nombre  varchar(100) null,
    evidencia_ruta    varchar(500) null,
    estado    varchar(10) null,
    obs_resp_corr    varchar(4000) null
);


CREATE TABLE SAC_ACCION_CORRECTIVA_EVIDENCIA (
	nombre varchar(50) primary key not null,
	ruta    varchar(500) null,
	sac_accion_correctiva_id numeric (10,0) not null
);

-- AYC


CREATE TABLE g_tipo_causa (
	g_tipo_causa_id numeric(10, 0) primary key not null,
    ayc varchar(50) null,
    descripcion varchar(200) null
);
CREATE TABLE g_nivel_riesgo (
	g_nivel_riesgo_id numeric(10, 0) primary key not null,
    nombre varchar(200) null
);

CREATE TABLE origen_ayc (
    name varchar(50),
    code varchar(50) primary key not null
);

CREATE TABLE tipo_riesgo_ayc (
    name varchar(50),
    code varchar(50) primary key not null
);


CREATE TABLE ayc_registro (
	ayc_registro_id   integer primary key autoincrement not null,
	origen varchar(1) null,
	g_tipo_causa_id numeric(10,0) null,
    g_tipo_causa_nombre varchar(500) null,
    fb_gerencia numeric (10,0) null,
    fb_gerencia_nombre varchar(500) null,
	fb_area_id numeric(10,0) null,
    fb_area_nombre varchar(500) null,
	descripcion    varchar(200) null,
	lugar    varchar(200) null,
	fecha varchar(200) null,
	hora    varchar(200) null,
	corrigio varchar(10) null,
	tipo_evento_id numeric(10,0) null,
    tipo_evento_nombre varchar(500) null,
	nivel_riesgo_id numeric(10,0) null,
    nivel_riesgo_nombre varchar(500) null,
	accion_ejec varchar(200) null,
    fb_empresa_especializada_id numeric(10,0) null,
    fb_empresa_especializada_nombre varchar(500) null,
    latitud varchar(200) null,
    longitud varchar(200) null,
    foto_pre_evento_nombre  varchar(100) null,
    foto_pre_evento_ruta    varchar(500) null,
    foto_evento_nombre  varchar(100) null,
    foto_evento_ruta    varchar(500) null,
	fb_empleado_id numeric(10,0) null,
    fb_empleado_nombre varchar(500) null,
    fb_uea_pe_id numeric(10,0) null,
    estado    varchar(10) null

);

CREATE TABLE ayc_evidencia (
	nombre  varchar(50) primary key not null,
    ruta    varchar(500) null,
	ayc_registro_id numeric (10,0) not null
);
CREATE TABLE ayc_reportante (
	fb_empleado_id  numeric (10,0) primary key not null,
	fb_uea_pe_id numeric (10,0) not null,
	nombreCompleto  varchar(50) not null,
    numero_documento    varchar(50) not null
);


-- Modulo Incidentes
CREATE TABLE inc_tipo_reporte (
	inc_tipo_reporte_id  numeric (10,0) primary key not null,
    nombre    varchar(500) null
);

CREATE TABLE inc_sub_tipo_reporte (
	inc_sub_tipo_reporte_id  numeric (10,0) primary key not null,
	inc_tipo_reporte_id  integer not null,
    nombre    varchar(500) null
);
CREATE TABLE inc_detalle_perdida (
	inc_segun_tipo_id  numeric (10,0) primary key not null,
	inc_tipo_reporte_id  integer not null,
    nombre    varchar(500) null,
    codigo    varchar(500) null
);
CREATE TABLE inc_potencial_perdida (
	inc_potencial_perdida_id  numeric (10,0) primary key not null,
    nombre    varchar(500) null,
    codigo    varchar(500) null
);
CREATE TABLE inc_incidente (
    inc_incidente_id integer primary key autoincrement not null,
    inc_tipo_evento numeric (10,0) null,
    inc_tipo_evento_nombre varchar(500) null,
    inc_sub_tipo_evento numeric (10,0) null,
    inc_sub_tipo_evento_nombre varchar(500) null,
    inc_segun_tipo numeric (10,0) null,
    inc_segun_tipo_nombre varchar(500) null,
    inc_potencial_perdida numeric (10,0) null,
    inc_potencial_perdida_nombre varchar(500) null,
    fb_gerencia numeric (10,0) null,
    fb_gerencia_nombre varchar(500) null,
    fb_area numeric (10,0) null,
    fb_area_nombre varchar(500) null,
    fecha_evento varchar(200) null,
    hora    varchar(200) null,
    lugar_evento varchar(200) null,
    descripcion_evento varchar(200) null,
    imagen_pre_evento_nombre  varchar(100) null,
    imagen_pre_evento_ruta    varchar(500) null,
    imagen_evento_nombre  varchar(100) null,
    imagen_evento_ruta    varchar(500) null,
    estado    varchar(10) null
);

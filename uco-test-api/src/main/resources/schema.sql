-- También protege bases existentes. Si hay duplicados, falla sin eliminar datos.
CREATE UNIQUE INDEX IF NOT EXISTS uk_nota_estudiante_evaluacion
    ON nota_estudiante (estudiante_grupo_id, nota_id);

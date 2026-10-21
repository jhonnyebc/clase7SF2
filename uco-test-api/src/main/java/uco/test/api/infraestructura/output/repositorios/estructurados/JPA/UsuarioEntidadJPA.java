package uco.test.api.infraestructura.output.repositorios.estructurados.JPA;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "usuario")
public class UsuarioEntidadJPA {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true)
    private String usuario;
    @Column(nullable = false)
    private String claveHash;
    @OneToOne(optional = false)
    @JoinColumn(name = "docente_id", nullable = false, unique = true)
    private DocenteEntidadJPA docente;

    protected UsuarioEntidadJPA() {}
    public UsuarioEntidadJPA(String usuario, String claveHash, DocenteEntidadJPA docente) {
        this.id = UUID.randomUUID();
        this.usuario = usuario;
        this.claveHash = claveHash;
        this.docente = docente;
    }
    public String getUsuario() { return usuario; }
    public String getClaveHash() { return claveHash; }
    public DocenteEntidadJPA getDocente() { return docente; }
}

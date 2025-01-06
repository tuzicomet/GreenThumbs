package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
//import org.hibernate.sql.results.graph.Fetch;

@Entity
public class Authority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "authority_id")
    private Long authorityId;

    @Column()
    private String role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private AbstractUser user;

    protected Authority() {

    }

    public Authority(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

}

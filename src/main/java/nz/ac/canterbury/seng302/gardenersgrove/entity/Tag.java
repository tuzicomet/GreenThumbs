package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TAG")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tagId")
    private Long tagId;

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    @Column(name = "content", nullable = false)
    private String content;
    @Column(name = "verified", nullable = false)
    private boolean verified;

    @ManyToMany(mappedBy = "tags", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    Set<Garden> gardens = new HashSet<>();

    /**
     * JPA required constructor
     */
    protected Tag() {}

    public Tag(String content, boolean verified) {
        this.content = content;
        this.verified=verified;
    }

    public Long getId() {
        return tagId;
    }

    public String getContent() {
        return content;
    }
    public Set<Garden> getGardens() {
        return gardens;
    }
}

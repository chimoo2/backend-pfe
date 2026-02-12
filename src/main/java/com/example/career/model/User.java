package com.example.career.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users") 
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String prenom;
    private String nom;

    @Column(unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private String password;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    private String resetToken;
    
    private String phone;
    private String company;
    private String position;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Document> documents;

    // Getter pour le CV le plus récent (pour compatibilité)
    public String getCvPath() {
        if (documents == null || documents.isEmpty()) return null;
        return documents.stream()
                .filter(d -> d.getDocumentType() == Document.DocumentType.CV)
                .findFirst()
                .map(Document::getFilePath)
                .orElse(null);
    }

    // Setter pour compatibilité (ne fait rien, utiliser documents à la place)
    public void setCvPath(String cvPath) {
        // Deprecated - use documents instead
    }
}

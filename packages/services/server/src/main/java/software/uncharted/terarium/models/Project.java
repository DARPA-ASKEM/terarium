package software.uncharted.terarium.models;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.time.Instant;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "projects")
public class Project extends PanacheEntity {

  @Column(nullable = false)
  public String name;

  @Lob
  public String description;

  @Column(name = "created_at")
  public Instant createdAt = Instant.now();

  @Column(name = "updated_at")
  public Instant updatedAt = Instant.now();
}

package ecommerce.web.app.entities;

import lombok.Data;

import jakarta.persistence.*;

@Data
@Entity
@Table(name = "favorites")
public class Favorites extends BaseEntity {

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "post_id")
    private Post post ;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_id")
    private User user ;
}

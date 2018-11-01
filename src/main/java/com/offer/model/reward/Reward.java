package com.offer.model.reward;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.offer.model.Game;
import com.offer.model.Offer;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "Rewards")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name ="type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue(value = "reward")
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;


    //@NotEmpty(message = "Please specify provider name for the offer")
    private String providerName;

    private Boolean allowAllGames;

    @OneToMany(
            mappedBy = "reward",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Game> games = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;



    public Reward() {

    }
    public Reward(String providerName, Boolean allowAllGames, ArrayList<Game> games) {
        this.providerName = providerName;
        this.allowAllGames = allowAllGames;
        this.games = games;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get provider name used as part of endpoint routing and id
     * @return String
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * This is to show provider name with first letter in uppercase.
     * @return
     */
    public String getProviderNameText() {
        return providerName.substring(0, 1).toUpperCase() + providerName.substring(1);
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public List<Game> getGames() {
        return this.games;
    }

    /**
     * Extract game Ids as Array List of strings from game list
     * @return ArrayList
     */
    @JsonIgnore
    public ArrayList<String> getGameIds() {
        ArrayList<String> gameIds = new ArrayList<>();
        this.games.forEach(game -> gameIds.add(game.getGameId()));
        return gameIds;
    }

    public void setGames(ArrayList<Game> games) {
        this.games = games;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public Boolean getAllowAllGames() {
        return allowAllGames;
    }

    public void setAllowAllGames(Boolean allowAllGames) {
        this.allowAllGames = allowAllGames;
    }
}

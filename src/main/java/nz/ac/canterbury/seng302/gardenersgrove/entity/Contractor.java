package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@DiscriminatorValue("CONTRACTOR")
public class Contractor extends AbstractUser{
    @Column(name = "about_me", length = 1024)
    private String aboutMe;

    @ElementCollection
    @CollectionTable(name = "contractor_pictures", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "work_pictures")
    private List<String> workPictures;

    @OneToOne
    private Location location;

    @Column(name = "num_ratings")
    private Integer numRatings =0;

    @Column(name = "rating_total")
    private Integer ratingTotal=0;


    public Contractor() {}

    //Use the user
    public Contractor(User user, String aboutMe, List<String> workPictures,Location location) {
        this.setUserId(user.getUserId());
        this.setFirstName(user.getFirstName());
        this.setLastName(user.getLastName());
        this.setEmail(user.getEmail());
        this.setPassword(user.getPassword());
        this.setDateOfBirth(user.getDateOfBirth());
        this.setProfilePicture(user.getProfilePicture());
        this.gardens = user.getOwnedGardens();
        this.aboutMe = aboutMe;
        this.workPictures = workPictures;
        this.location = location;
        this.numRatings = 0;
        this.ratingTotal = 0;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public Integer getRatingTotal() {
        if (this.ratingTotal == null) {
            return 0;
        }
        return this.ratingTotal;
    }

    public Integer getNumRatings() {
        if (this.numRatings == null) {
            return 0;
        }
        return this.numRatings;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public List<String>  getWorkPictures() {
        return workPictures;
    }

    public void setWorkPictures(List<String>  workPictures) {
        this.workPictures = workPictures;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void addRating(int rating) throws IllegalArgumentException{
        if(rating > 5 || rating < 0){
            throw new IllegalArgumentException();
        }
        this.numRatings = getNumRatings() +1;
        this.ratingTotal = getRatingTotal() + rating;
    }
    public Double getAverageRating(){
        if(this.getNumRatings() == 0){return null;}
        return ((double) this.getRatingTotal() / this.getNumRatings());
    }

    public Float getAverageRatingRounded(){
        if(this.getNumRatings() == 0){return null;}
        long round =  Math.round(((double) this.getRatingTotal()*10 / this.getNumRatings()));
        return (float)round/10;
    }

    public void setRatingTotal(Integer total){
        this.ratingTotal = total;
    }
    public void setNumRatings(Integer num){
        this.numRatings = num;
    }

}

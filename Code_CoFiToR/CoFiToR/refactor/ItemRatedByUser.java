package refactor;

public class ItemRatedByUser {
    private int userId;
    private int itemId;
    private float ratings;

    public ItemRatedByUser(int userId, int itemId, float ratings) {
        this.userId = userId;
        this.itemId = itemId;
        this.ratings = ratings;
    }

    public int getUserId() {
        return userId;
    }

    public int getItemId() {
        return itemId;
    }

    public float getRatings() {
        return ratings;
    }
}

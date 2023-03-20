public class User {
    private String username;
    private boolean status;

    public User(String username) {
        this.username = username;
        status= true;
    }

    public String getUsername() {
        return username;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}

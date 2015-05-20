package zumma.com.ninegistapp.model;

public class Data {
    private byte[] image;
    private String title;
    private int mes_count;

    public Data(String title, byte[] profile_pics) {
        this.title = title;
        this.image = profile_pics;
    }

    public Data(String title, int mes_count, byte[] profile_pics) {
        this.title = title;
        this.mes_count = mes_count;
        this.image = profile_pics;
    }

    public int getMes_count() {
        return mes_count;
    }

    public void setMes_count(int mes_count) {
        this.mes_count = mes_count;
    }

    public byte[] getImage() {
        return this.image;
    }

    public String getTitle() {
        return this.title;
    }


    public void setImage(byte[] image) {
        this.image = image;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

/* Location:           C:\Users\Okafor\workspace\imate\iMate_dex2jar.jar
 * Qualified Name:     com.imate.model.Data
 * JD-Core Version:    0.6.0
 */
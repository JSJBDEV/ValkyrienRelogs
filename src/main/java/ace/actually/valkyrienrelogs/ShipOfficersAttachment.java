package ace.actually.valkyrienrelogs;

public class ShipOfficersAttachment {
    private String[] officers;
    public ShipOfficersAttachment()
    {
        officers=new String[0];
    }
    public ShipOfficersAttachment(String[] officers)
    {
        this.officers=officers;
    }

    public String[] getOfficers() {
        return officers;
    }
}

public abstract class MessageBase {

    /** Turns the MessageClientToManager to string */
    public abstract String stringifyUsingJSON();

    /** This is for debug purpose */
    @Override
    public abstract String toString();
}

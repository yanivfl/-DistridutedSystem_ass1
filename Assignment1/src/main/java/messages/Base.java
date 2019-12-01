package messages;

public abstract class Base {

    /** Turns the messages.MessageClientToManager to string */
    public abstract String stringifyUsingJSON();

    /** This is for debug purpose */
    @Override
    public abstract String toString();
}

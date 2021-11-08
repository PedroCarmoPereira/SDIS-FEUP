class MessageSender implements Runnable {

    ChannelType channelType;
    Message message;

    public MessageSender(ChannelType channelType, Message message) {
        this.channelType = channelType;
        this.message = message;
    }

    @Override
    public void run() {
        switch (this.channelType) {
            case BACKUP:
                Peer.getMDB().send(this.message.getMessage());
                break;
            case CONTROL:
                Peer.getMC().send(this.message.getMessage());
                break;
            case RESTORE:
                Peer.getMDR().send(this.message.getMessage());
                break;
            default:
                break;

        }

    }
}
class ProcessReceived extends Thread {

    Object obj;

    public ProcessReceived(Object obj) {
        this.obj = obj;
    }

    @Override
    public void run() {
        String type = this.obj.getClass().getName();
        switch (type) {
            case "Join":
                Join join = (Join) obj;
                join.receive();
                break;
            case "Lookup":
                Lookup l = (Lookup) obj;
                l.findSucessor();
                break;
            case "LookupConfirmation":
                LookupConfirmation lc = (LookupConfirmation) obj;
                lc.receive();
                break;
            case "GetPredecessor":
                GetPredecessor gp = (GetPredecessor) obj;
                gp.receive();
                break;
            case "PossiblePredecessor":
                PossiblePredecessor pp = (PossiblePredecessor) obj;
                pp.receive();
                break;
            case "PossibleSuccessor":
                PossibleSuccessor ps = (PossibleSuccessor) obj;
                ps.receive();
                break;
            case "java.lang.String":
                    System.out.print((String) obj);
                break;
            case "JoinConfirmation":
                JoinConfirmation confirm = (JoinConfirmation) obj;
                confirm.receive();
                break;
            case "LeavePredecessor":
                LeavePredecessor lp = (LeavePredecessor) obj;
                lp.receive();
                break;
            case "LeaveSuccessor":
                LeaveSuccessor ls = (LeaveSuccessor) obj;
                ls.receive();
                break;
            case "RestoreRequest":
                RestoreRequest rr = (RestoreRequest) obj;
                rr.receive();
                break;
            case "Restore":
                Restore r = (Restore) obj;
                r.receive();
                break;
            case "Delete":
                Delete d = (Delete) obj;
                d.receive();
                break;
            case "BackupRequest":
                BackupRequest br = (BackupRequest) obj;
                br.receive();
                break;
            case "BackupRequestResponse":
                BackupRequestResponse brr = (BackupRequestResponse) obj;
                brr.receive();
                break;
            case "Backup":
                Backup backup = (Backup) obj;
                backup.receive();
                break;
            case "CatchUp":
                CatchUp cp = (CatchUp) obj;
                cp.receive();;
                break;
            case "Up":
                Up up = (Up) obj;
                up.receive();;
                break;
            default:
                System.out.println("Class " + type + " not processed.");
                break;
        }
        return;
    }
}
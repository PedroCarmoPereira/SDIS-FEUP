# sdis1920-t4g07
Pedro Carmo Pereira     201708807
Pedro Hugo Noevo        201604725

## PEERS
./peer.sh < RMIport > < PeerId > < MChost > < MCport > < MDBhost > < MDBport > < MDRhost > < MDRport >

### INITIATOR PEER
./peer.sh 4545 1 224.0.0.15 8001 225.0.0.0 4445 224.0.0.0 4446

### OTHER PEERS
./peer.sh < N != 4545 (if runing in the same pc)> < Id != other peers ids > 224.0.0.15 8001 225.0.0.0 4445 224.0.0.0 4446

## CLIENT
./run.sh < IP address >:< port number > < Peer Id > < sub_protocol > < opnd_1 > < opnd_2 >
if (< IP address > == null) < IP address > is considered "localhost"

### BACKUP
./run.sh 4545 1 backup ./testFiles/1.pdf 2

### RESTORE
./run.sh 4545 1 restore ./testFiles/1.pdf

### DELETE
./run.sh 4545 1 delete ./testFiles/1.pdf

### RECLAIM
./run.sh 4545 1 reclaim -1

### STATE
./run.sh 4545 1 state




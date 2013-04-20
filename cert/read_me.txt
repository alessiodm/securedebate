--------------------------------------------------------------------
CLIENT_CERTS:
--------------------------------------------------------------------
keystore del client, contenente i certificati che ne attestano
l'identità.
pwd: clientks

KEYS:
clientk			PWD: clientk

--------------------------------------------------------------------
CLIENT_TRUST_CA_CERTS:
--------------------------------------------------------------------
truststore del client, contenente i certificati delle certification
authorities e dei corrispondenti fidati.
pwd: clientts

--------------------------------------------------------------------
SERVER_CERTS:
--------------------------------------------------------------------
keystore del server, contenente i certificati che ne attestano
l'identità.
pwd: serverks


KEYS:
serverk			PWD: serverk

--------------------------------------------------------------------
SERVER_TRUST_CERTS:
--------------------------------------------------------------------
truststore del server, contenente i certificati dei client fidati.
pwd: serverts
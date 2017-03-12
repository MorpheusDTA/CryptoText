# CryptoText

     !!!!!     WARNING     !!!!!
	This project is an engineering school project, it should not be used for professional
purposes where encryption is necesary. This should not be considered as a fully-secure app
meeting with high security standards.

	This is an Android app project to develop an app that enable users to communicate using 
encrypted text messages. The aim is to encrypt the SMS using AES-CBC-256 and to share the keys
using an encryption protocol relying on asymmetric encryption such as RSA. The best way would
be to use an authenticated server storing all the public keys while the users would store on
their smartphones their own private keys.

 	In the current version, the app is able to send, receive and decrypt encrypted text
messages. However, no key sharing protocol is implemented yet. Consequently, the Base64
representation of the key is to be shared through an other media and re-entere in the adequate
field. The received text messages are stored encrypted on the smartphone usual database for SMS
and can be decrypted only using CryptoText and clicking on the desired SMS providing your
KeyStore password.

	The app is not present on Google Play yet, hope it will be one day. Consequently it is
to be installed using the development mode of any Android smartphone using the .apk file at the
last release.

	Please, feel free to make suggestions, raise issues, fork it or ask for pull
requests or commits.
	Thanks.

#!/bin/bash

# Must be run from cygwin shell window.
# From within eclipse will result in
#   cp: command not found

cp ../export/sudoku-bin-1.0.1.jar .

jarsigner -keystore myKeystore sudoku-bin-1.0.1.jar myself

mv sudoku-bin-1.0.1.jar ../export/sudoku-bin-1.0.1-signed.jar



--------------------------------------------------------------------------
TODO: find out about -tsa or -tsacert

$ jarsigner -keystore myKeystore sudoku-bin-1.0.1.jar myself
Enter Passphrase for keystore: password
jar signed.

Warning:
The signer certificate will expire within six months.
No -tsa or -tsacert is provided and this jar is not timestamped. Without a
timestamp, users may not be able to validate this jar after the signer
certificate's expiration date (2015-07-06) or after any future revocation date.
--------------------------------------------------------------------------


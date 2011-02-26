All text within the comments /* */  will be
ignored by the application.  It is highly recommended to leave appropriate
comments in your raw commands for others to reference at a later time.

The config allows variable substitution with any alpha-numeric word beginning
with a "$" dollar sign.  For example: $var1, $name, $barcode, are all valid
variable names, so as long as they are provided to the application upon loading.

The most common way to load jZebra is through an html applet.  For a sample see
"preview-application.html".  The $barcode parameter is supplied as follows:
[code] <param name="barcode" value="01234567890">

Additionally, jZebra can take variables via command.  The $barcode parameter is
supplied as follows:
[code] #> java -jar jzebra.jar barcode=01234567890

In addition to supplying variable substitution, jZebra accepts some built-in
parameters that allow this config file to be overridden as well as some
localization overrides and auto-printing.
[code] #>  java -jar jzebra.jar config=/home/bob/test.txt successmessage=¡Bueno!

Although the sample below uses variables "$FIRSTNAME", "$LASTNAME".  These
variables can be safely replaced with variables of your own choice.  The file is
for an Zebra P330i card printer, and will need to be re-writen for your model.

Remember to change your config file to refect the variable names.  jZebra's
console's output will give you more verbose messages, which can be enabled via
Windows Control Panel --> Java --> Advanced --> Java Console --> Enable Console
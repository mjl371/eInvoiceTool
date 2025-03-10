jpackage --type exe  --input target/ --dest release --name InvoiceTool --main-jar einvoice-1.0.jar --main-class com.einvoice.InvoiceApp --win-console --win-shortcut 

jpackage --type app-image --input target/ --dest ./target/jpackage -m "com.einvoice.InvoiceApp" -n InvoiceTool --module-path ".\target\einvoice-1.0.jar"

C:\CommandLineTools\Java\jdk-21\bin\jpackage.exe --type app-image -n JavaFXSample --module-path ".\target\JavaFX-Package-Sample-1.0.0.jar;.\target\alternateLocation\" -m "sample/com.icuxika.MainApp" --icon ./src/main/resources/application.ico --app-version 1.0.0 --dest .\target\jpackage-direct-build-dir
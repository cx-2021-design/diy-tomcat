del /q bootstrap.jar
jar cvf0 bootstrap.jar -C bin org/example/diytomcat/Bootstrap.class -C bin org/example/diytomcat/classloader/CommonClassLoader.class
del /q lib/diytomcat.jar
cd bin
jar cvf0 ../lib/diytomcat.jar *
cd ..
java -cp bootstrap.jar org.example.diytomcat.Bootstrap
pause
# How to install
git clone https://github.com/iot-senz/bankz.git BankZ

Make changes to the 'RemoteSenzService' file where you need to set the host address of the
senzswitch. set SENZ_HOST to the ip address as necessary .

Printing structure is in the 'PrintUtils' file. Edit it as necessary.
When editing the file logo. There is a process.
You shold first get the image in low resolution.
Then make it bw.
then create a byte array from the image.
make a hex string from the image.
you could do these things in Matlab easily.

A=imread('SDB-logo-english-620x360.jpg'); //read image *.jpg
B=rgb2bw(A,0.9); // convert to black and white
C=imresize(B,0.8); // resize image
E=imcomplement(C); // get complement value to print
D=binaryVectorToHex(E); // convert binary value to hex

After doing all these . structuring thestring also is a tricky part.
read the structuring string from the comments on the 'printLogoCommand' variable.

package isp.steganography;

import fri.isp.Agent;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.BitSet;

/**
 * Assignments:
 * <p>
 * 1. Change the encoding process, so that the first 4 bytes of the steganogram hold the
 * length of the payload. Then modify the decoding process accordingly.
 * 2. Add security: Provide secrecy and integrity for the hidden message. Use GCM for cipher.
 * Also, use AEAD to provide integrity to the steganogram size.
 * 3. Optional: Enhance the capacity of the carrier:
 * -- Use the remaining two color channels;
 * -- Use additional bits.
 */
public class ImageSteganography {

    public static void main(String[] args) throws Exception {
        final byte[] payload = "My secret size".getBytes(StandardCharsets.UTF_8);
        
        ImageSteganography.encode(payload, "images/1_Kyoto.png", "images/steganogram.png");
        final byte[] decoded = ImageSteganography.decode("images/steganogram.png", payload.length, false);
        System.out.printf("Decoded1: %s%n", new String(decoded, StandardCharsets.UTF_8));


        // TODO: Assignment 1
        final byte[] paylen = ByteBuffer.allocate(4).putInt(payload.length).array();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(paylen);
        //System.out.printf("Paylen: %s%n", Agent.hex(paylen));
        outputStream.write(payload);
        //System.out.printf("Payload: %s%n", Agent.hex(payload));
        byte c[] = outputStream.toByteArray();
        //System.out.printf("Con: %s%n", new String(c,StandardCharsets.UTF_8));
        ImageSteganography.encode(c,"images/1_Kyoto.png", "images/steganogram.png");
        final byte[] decoded1 = ImageSteganography.decode("images/steganogram.png", c.length, true);
        //System.out.printf("Decoded: %s%n", new String(decoded1, "UTF-8"));
        //ImageSteganography.encode(payload, "images/1_Kyoto.png", "images/steganogram.png");
        //final byte[] decoded1 = ImageSteganography.decode("images/steganogram.png");
        System.out.printf("Decoded2: %s%n", new String(decoded1, "UTF-8"));


        // TODO: Assignment 2
        /*
        final SecretKey key = KeyGenerator.getInstance("AES").generateKey();
        final byte[] iv = ImageSteganography.encryptAndEncode(c, "images/2_Morondava.png", "images/steganogram-encrypted.png", key);
        final byte[] decoded2 = ImageSteganography.decryptAndDecode("images/steganogram-encrypted.png", key, iv);
        System.out.printf("Decoded3: %s%n", new String(decoded2, "UTF-8"));
                 */
    }

    /**
     * Encodes given payload into the cover image and saves the steganogram.
     *
     * @param pt      The payload to be encoded
     * @param inFile  The filename of the cover image
     * @param outFile The filename of the steganogram
     * @throws IOException If the file does not exist, or the saving fails.
     */
    public static void encode(final byte[] ct, final String inFile, final String outFile) throws IOException {
        // load the image
        final BufferedImage image = loadImage(inFile);

        // Convert byte array to bit sequence
        final BitSet bits = BitSet.valueOf(ct);

        // encode the bits into image
        encodeBits(bits, image);

        // save the modified image into outFile
        saveImage(outFile, image);
    }

    /**
     * Decodes the message from given filename.
     *
     * @param fileName The name of the file
     * @return The byte array of the decoded message
     * @throws IOException If the filename does not exist.
     */
    public static byte[] decode(final String fileName, int size, boolean w_len) throws IOException {
        // load the image
        final BufferedImage image = loadImage(fileName);

        // read all LSBs
        final BitSet bits = decodeBits(image, size, w_len);

        // convert them to bytes
        return bits.toByteArray();
    }

    /**
     * Encrypts and encodes given plain text into the cover image and then saves the steganogram.
     *
     * @param pt      The plaintext of the payload
     * @param inFile  cover image filename
     * @param outFile steganogram filename
     * @param key     symmetric secret key
     * @throws Exception
     */
    public static byte[] encryptAndEncode(final byte[] pt, final String inFile, final String outFile, final Key key)
            throws Exception {
        // Encryption
        final Cipher gcm = Cipher.getInstance("AES/GCM/NoPadding");
        gcm.init(Cipher.ENCRYPT_MODE, key);
        System.out.printf("pt before encrypt: %s %n", new String(pt));
        System.out.printf("pt before encrypt: %s %n", Agent.hex(pt));
        final byte[] ct = gcm.doFinal(pt);
        System.out.printf("ct after encrypt: %s %n", Agent.hex(ct));
        final byte[] iv = gcm.getIV();

        // Encoding into file
        encode(pt, inFile, outFile);

        return iv;
    }

    /**
     * Decrypts and then decodes the message from the steganogram.
     *
     * @param fileName name of the steganogram
     * @param key      symmetric secret key
     * @return plaintext of the decoded message
     * @throws Exception
     */
    public static byte[] decryptAndDecode(final String fileName, final Key key, final byte[] iv) throws Exception {
        // decode
        // Encoding into file
        final byte[] decoded = decode(fileName, 0,true);
        System.out.println(Agent.hex(decoded));

        // decrpyt
        final Cipher gcm = Cipher.getInstance("AES/GCM/NoPadding");
        final GCMParameterSpec specs = new GCMParameterSpec(128, iv);
        gcm.init(Cipher.DECRYPT_MODE, key, specs);
        final byte[] pt = gcm.doFinal(decoded);

        return pt;
    }

    /**
     * Loads an image from given filename and returns an instance of the BufferedImage
     *
     * @param inFile filename of the image
     * @return image
     * @throws IOException If file does not exist
     */
    protected static BufferedImage loadImage(final String inFile) throws IOException {
        return ImageIO.read(new File(inFile));
    }

    /**
     * Saves given image into file
     *
     * @param outFile image filename
     * @param image   image to be saved
     * @throws IOException If an error occurs while writing to file
     */
    protected static void saveImage(String outFile, BufferedImage image) throws IOException {
        ImageIO.write(image, "png", new File(outFile));
    }

    /**
     * Encodes bits into image. The algorithm modifies the least significant bit
     * of the red RGB component in each pixel.
     *
     * @param payload Bits to be encoded
     * @param image   The image onto which the payload is to be encoded
     */
    protected static void encodeBits(final BitSet payload, final BufferedImage image) {
        for (int x = image.getMinX(), bitCounter = 0; x < image.getWidth() && bitCounter < payload.size(); x++) {
            for (int y = image.getMinY(); y < image.getHeight() && bitCounter < payload.size(); y++) {
                final Color original = new Color(image.getRGB(x, y));

                // Let's modify the red component only
                final int newRed = payload.get(bitCounter) ?
                        original.getRed() | 0x01 : // sets LSB to 1
                        original.getRed() & 0xfe;  // sets LSB to 0

                //System.out.printf("On position: (%s, %s), bit %s is written %n", x, y, payload.get(bitCounter) ? 1 : 0);
                // Create a new color object
                final Color modified = new Color(newRed, original.getGreen(), original.getBlue());

                // Replace the current pixel with the new color
                image.setRGB(x, y, modified.getRGB());

                // Uncomment to see changes in the RGB components
                // System.out.printf("%03d bit [%d, %d]: %s -> %s%n", bitCounter, x, y, original, modified);

                bitCounter++;
            }
        }
    }

    /**
     * Decodes the message from the steganogram
     *
     * @param image steganogram
     * @param size  the size of the encoded steganogram
     * @return {@link BitSet} instance representing the sequence of read bits
     */
    protected static BitSet decodeBits(final BufferedImage image, int size, boolean w_len) {
        final BitSet bits = new BitSet();
        final BitSet sbits = new BitSet();
        // set sizeBits according to the first 32 bits
        int bitCounter = 0;
        int ystart = image.getMinY();
        if (w_len) {
            for (int y = image.getMinY(); y < image.getHeight() && bitCounter < 32; y++) {
                final Color color = new Color(image.getRGB(0, y));
                final int lsb = color.getRed() & 0x01;
                //System.out.println(lsb == 0x01);
                sbits.set(bitCounter, lsb == 0x01);
                bitCounter++;
            }
            final byte[] jup = sbits.toByteArray();
            //System.out.println(jup);
            //System.out.printf("size in hex: %s %n", Agent.hex(jup));
            size = ByteBuffer.allocate(4).put(jup).getInt(0);
            ystart += 32; // first 32 bits is the payload size
        }
        System.out.printf("Payload size is: %s bytes/characters %n", size);
        final int sizeBits = 8 * size;

        bitCounter = 0;
        for (int x = image.getMinX(); x < image.getWidth() && bitCounter < sizeBits; x++) {
            for (int y = ystart; y < image.getHeight() && bitCounter < sizeBits; y++) {
                final Color color = new Color(image.getRGB(x, y));
                final int lsb = color.getRed() & 0x01;
                bits.set(bitCounter, lsb == 0x01);
                bitCounter++;
            }
            //System.out.println(bitCounter);
        }

        return bits;
    }
}
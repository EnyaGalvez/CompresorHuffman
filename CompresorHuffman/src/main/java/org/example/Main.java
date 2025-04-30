package org.example;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        // Verificar argumentos de línea de comandos
        if (args.length != 3) {
            printUsage();
            return;
        }

        String mode = args[0];
        String inputFileArg = args[1];
        String outputFileArg = args[2];

        HuffmanLogic logic = new HuffmanLogic();

        try {
            if ("compress".equalsIgnoreCase(mode)) {
                String inputFile = inputFileArg; // El archivo de entrada es directamente el argumento
                String outputFileBase = outputFileArg; // La base para los archivos de salida
                logic.compress(inputFile, outputFileBase);
                System.out.println("Compression successful!");
                System.out.println("Output files: " + outputFileBase + ".huff, " + outputFileBase + ".hufftree");
            } else if ("decompress".equalsIgnoreCase(mode)) {
                String inputFileBase = inputFileArg; // La base para los archivos de entrada
                String outputFile = outputFileArg; // El archivo de salida .txt
                String huffFile = inputFileBase + ".huff";
                String treeFile = inputFileBase + ".hufftree";
                logic.decompress(huffFile, treeFile, outputFile);
                System.out.println("Decompression successful!"); 
                System.out.println("Output file: " + outputFile);
            } else {
                System.err.println("Error: Invalid mode '" + mode + "'. Use 'compress' or 'decompress'.");
                printUsage();
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: Input file not found - " + e.getMessage());
        } catch (IOException e) {
            System.err.println("An error occurred during processing: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar huffman-compressor-1.0-SNAPSHOT.jar <compress|decompress> <inputFile> <outputFile>");
        System.out.println("\nExample (Compress):");
        System.out.println("  java -jar huffman-compressor-1.0-SNAPSHOT.jar compress my_document.txt compressed_data");
        System.out.println("  (Reads 'my_document.txt', creates 'compressed_data.huff' and 'compressed_data.hufftree')");
        System.out.println("\nExample (Decompress):");
        System.out.println("  java -jar huffman-compressor-1.0-SNAPSHOT.jar decompress compressed_data original_document.txt");
        System.out.println("  (Reads 'compressed_data.huff', 'compressed_data.hufftree', creates 'original_document.txt')");
    }
}
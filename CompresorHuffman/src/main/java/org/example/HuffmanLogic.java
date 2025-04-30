package org.example;

import java.io.*;
import java.util.*;

public class HuffmanLogic {
    // Compresion

    public void compress(String inputFile, String outputFileBase) throws IOException {
        String text = readFileContent(inputFile);
        if (text.isEmpty()) {
            // Crea archivos vacíos si el input está vacío
            createEmptyFile(outputFileBase + ".huff");
            createEmptyFile(outputFileBase + ".hufftree");
            return; // No hay nada que comprimir
        }

        Map<Character, Integer> frequencies = calculateFrequencies(text);
        HuffmanNode root = buildHuffmanTree(frequencies);
        Map<Character, String> huffmanCodes = generateCodes(root);
        String encodedText = encodeText(text, huffmanCodes);

        writeCompressedFile(encodedText, outputFileBase + ".huff");
        saveTree(root, outputFileBase + ".hufftree");
    }

    private String readFileContent(String filePath) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        // Usar try-with-resources asegura que el reader se cierre
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {
            int c;
            while ((c = reader.read()) != -1) {
                contentBuilder.append((char) c);
            }
        } // El reader se cierra automaticamente aqui, incluso si hay excepcion
        return contentBuilder.toString();
    }

    private void createEmptyFile(String filePath) throws IOException {
        new FileOutputStream(filePath).close();
    }


    private Map<Character, Integer> calculateFrequencies(String text) {
        Map<Character, Integer> frequencies = new HashMap<>();
        for (char character : text.toCharArray()) {
            frequencies.put(character, frequencies.getOrDefault(character, 0) + 1);
        }
        return frequencies;
    }

    private HuffmanNode buildHuffmanTree(Map<Character, Integer> frequencies) {
        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>();

        for (Map.Entry<Character, Integer> entry : frequencies.entrySet()) {
            priorityQueue.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        // Caso especial: archivo con un solo tipo de caracter
        if (priorityQueue.size() == 1) {
            HuffmanNode single = priorityQueue.peek();
            return new HuffmanNode(single.frequency, single, new HuffmanNode('\1', 0)); // Nodo derecho dummy simple
        }


        while (priorityQueue.size() > 1) {
            HuffmanNode left = priorityQueue.poll();
            HuffmanNode right = priorityQueue.poll();
            HuffmanNode merged = new HuffmanNode(left.frequency + right.frequency, left, right);
            priorityQueue.add(merged);
        }

        return priorityQueue.poll();
    }

    private Map<Character, String> generateCodes(HuffmanNode root) {
        Map<Character, String> huffmanCodes = new HashMap<>();
        // Si el arbol es nulo o es un nodo interno sin hijos (no debería pasar con buildTree)
        if (root == null || (!root.isLeaf() && root.left == null && root.right == null)) {
            return huffmanCodes; // Devuelve mapa vacío
        }
        generateCodesRecursive(root, "", huffmanCodes);
        return huffmanCodes;
    }

    private void generateCodesRecursive(HuffmanNode node, String currentCode, Map<Character, String> huffmanCodes) {
        if (node == null) {
            return;
        }

        if (node.isLeaf()) {
            // Asegurarse de no añadir codigos para caracteres nulos
            if (node.character != '\0' && node.character != '\1') {
                // Caso especial: árbol de un solo nodo útil (manejado en buildTree)
                // El código deberia ser '0' si es el hijo izquierdo del dummy
                if (currentCode.isEmpty()) {
                    huffmanCodes.put(node.character, "0");
                } else {
                    huffmanCodes.put(node.character, currentCode);
                }
            }
            return;
        }

        generateCodesRecursive(node.left, currentCode + "0", huffmanCodes);
        generateCodesRecursive(node.right, currentCode + "1", huffmanCodes);
    }

    private String encodeText(String text, Map<Character, String> huffmanCodes) {
        StringBuilder encodedText = new StringBuilder();
        for (char character : text.toCharArray()) {
            String code = huffmanCodes.get(character);
            if (code != null) { // Verifica si el caracter tiene código (debería tenerlo)
                encodedText.append(code);
            } else {

            }
        }
        return encodedText.toString();
    }

    private void writeCompressedFile(String encodedText, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            int numBits = encodedText.length();
            int padding = (8 - (numBits % 8)) % 8;
            bos.write(padding); // Escribe la info de padding (0-7)

            int bitIndex = 0;
            while (bitIndex < numBits) {
                int currentByteValue = 0;
                for (int i = 0; i < 8 && bitIndex < numBits; i++) {
                    currentByteValue <<= 1; // Desplaza a la izquierda
                    if (encodedText.charAt(bitIndex) == '1') {
                        currentByteValue |= 1; // Pone el bit menos significativo a 1
                    }
                    bitIndex++;
                }
                // Si se sale del bucle for antes de completar 8 bits se desplaza a la izquierda para alinear correctamente antes de escribir, añadiendo los bits de padding implícitamente (serán 0)
                if (bitIndex == numBits && numBits % 8 != 0) {
                    currentByteValue <<= padding;
                }

                bos.write(currentByteValue);
            }
            // Si el texto codificado está vacio, solo se escribe el padding
            if (numBits == 0){
                bos.write(0); // Escribe un byte de padding 0 si no hay bits
            }
        }
    }

    // Guardado del Árbol

    // 'I' para Nodo Interno, 'L' para Nodo Hoja seguido del caracter
    private void saveTree(HuffmanNode root, String filePath) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filePath)))) {
            if (root == null) {
                return;
            }
            writeTreeRecursive(root, dos);
        }
    }

    private void writeTreeRecursive(HuffmanNode node, DataOutputStream dos) throws IOException {
        if (node == null) {
            return;
        }
        if (node.isLeaf()) {
            if (node.character != '\1') {
                dos.writeChar('L'); // Marcador de Hoja
                dos.writeChar(node.character);
            }
        } else {
            dos.writeChar('I'); // Marcador de Nodo Interno
            writeTreeRecursive(node.left, dos);
            writeTreeRecursive(node.right, dos);
        }
    }

    private HuffmanNode loadTree(String filePath) throws IOException {
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)))) {
            // Verificar si el archivo está vacío antes de intentar leer
            if (dis.available() == 0) {
                return null; // Archivo de árbol vacío
            }
            return readTreeRecursive(dis);
        } catch (EOFException e) {
            throw new IOException("Error reading tree file: Unexpected end of file.", e);
        }
    }

    private HuffmanNode readTreeRecursive(DataInputStream dis) throws IOException {
        // Lee el marcador ('I' o 'L')
        char marker;
        try {
            marker = dis.readChar();
        } catch (EOFException eof) {
            // Fin de archivo inesperado mientras se leía el marcador
            throw new IOException("Corrupted tree file: unexpected end while reading node marker.", eof);
        }


        if (marker == 'L') {
            char character;
            try {
                character = dis.readChar();
            } catch (EOFException eof) {
                // Fin de archivo inesperado mientras se leía el caracter
                throw new IOException("Corrupted tree file: unexpected end while reading leaf character.", eof);
            }
            return new HuffmanNode(character, 0);
        } else if (marker == 'I') {
            HuffmanNode left = readTreeRecursive(dis);
            HuffmanNode right = readTreeRecursive(dis);
            return new HuffmanNode(0, left, right);
        } else {
            // Indica que se encontró un marcador invalido
            throw new IOException("Corrupted tree file: Invalid node marker found '" + marker + "'");
        }
    }


    // Descompresion

    public void decompress(String huffFile, String treeFile, String outputFile) throws IOException {
        HuffmanNode root = loadTree(treeFile);
        // Si loadTree devuelve null o el arbol reconstruido es invalido
        if (root == null) {
            // Crear archivo de salida vacío si el árbol no se pudo cargar
            createEmptyFile(outputFile);
            return;
        }

        byte[] compressedData = readCompressedFileBytes(huffFile);
        if (compressedData.length <= 1) {
            createEmptyFile(outputFile);
            return;
        }
        String decodedText = decodeBytes(compressedData, root);
        // Guardar texto decodificado
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"))) {
            writer.write(decodedText);
        }
    }

    private byte[] readCompressedFileBytes(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            return new byte[0]; // Devuelve array vacio si no existe o está vacío
        }
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            return baos.toByteArray();
        }
    }


    private String decodeBytes(byte[] compressedData, HuffmanNode root) throws IOException {
        if (compressedData == null || compressedData.length == 0) {
            return ""; // Nada que decodificar
        }
        if (root == null) {
            throw new IOException("Cannot decode: Huffman tree is null.");
        }
        StringBuilder decodedText = new StringBuilder();
        int padding = compressedData[0] & 0xFF; // Obtener el conteo de padding (0-7)

        // Caso especial: arbol con un solo nodo util
        boolean singleCharTree = root.isLeaf() || (root.left != null && root.left.isLeaf() && root.right != null && root.right.character == '\1'); // Verifica si es el árbol dummy de un solo caracter

        if (singleCharTree) {
            if (root.isLeaf()) {
                // TODO: Manejar este caso si es posible (requiere longitud original)
                throw new IOException("Decoding single-node tree without structure requires original length (not implemented).");
            }
        }

        HuffmanNode currentNode = root;
        for (int i = 1; i < compressedData.length; i++) {
            int currentByte = compressedData[i] & 0xFF; // Leer byte como entero sin signo

            // Procesar cada bit del byte, excepto los de padding en el último byte
            int bitsToProcess = 8;
            if (i == compressedData.length - 1) { // Si es el último byte
                bitsToProcess = 8 - padding;
            }

            for (int j = 7; j >= (8 - bitsToProcess); j--) {
                int bit = (currentByte >> j) & 1;

                if (currentNode == null) {
                    throw new IOException("Decoding error: unexpected null node reached in tree traversal.");
                }

                if (bit == 0) {
                    currentNode = currentNode.left;
                } else { // bit == 1
                    currentNode = currentNode.right;
                }

                if (currentNode != null && currentNode.isLeaf()) {
                    if(currentNode.character != '\1') {
                        decodedText.append(currentNode.character);
                    }
                    currentNode = root; // Volver a la raíz para el siguiente caracter
                } else if (currentNode == null) {
                    // Esto indica un error en los datos comprimidos o el arbol
                    throw new IOException("Decoding error: Invalid path in Huffman tree encountered. Compressed data might be corrupt.");
                }
            }
        }

        // Si después de procesar todos los bits no se termina en la raiz, indica si hay un archivo corrupto.
        if (currentNode != root && currentNode != null) {
            System.err.println("Warning: Decoding finished mid-traversal. File might be truncated or corrupt.");
        }


        return decodedText.toString();
    }
}

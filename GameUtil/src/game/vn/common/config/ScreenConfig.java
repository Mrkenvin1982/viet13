/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.config;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author hanv
 */
public class ScreenConfig {
    private static final Path PATH = FileSystems.getDefault().getPath("conf", "screen.json");
    
    public static String getListScreen() {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(PATH);
            return new String(bytes);
        } catch (IOException ex) {
        }
        return null;
    }
}

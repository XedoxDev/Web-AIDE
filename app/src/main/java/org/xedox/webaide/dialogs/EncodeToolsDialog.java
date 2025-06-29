package org.xedox.webaide.dialogs;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import org.xedox.webaide.R;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncodeToolsDialog {

    public static void show(@NonNull Context context) {
        DialogBuilder dialogBuilder = new DialogBuilder(context)
                .setView(R.layout.dialog_encode_tools)
                .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss());

        EditText inputField = dialogBuilder.findViewById(R.id.input_text);
        EditText outputField = dialogBuilder.findViewById(R.id.output_text);
        Spinner methodSpinner = dialogBuilder.findViewById(R.id.method_spinner);
        Button processButton = dialogBuilder.findViewById(R.id.process_button);
        EditText aesKeyField = dialogBuilder.findViewById(R.id.aes_key);

        // Default: sembunyikan AES key input
        aesKeyField.setVisibility(View.GONE);

        List<String> methods = Arrays.asList(
                "Base64 Encode", "Base64 Decode",
                "URL Encode", "URL Decode",
                "MD5 Hash", "SHA-256 Hash",
                "AES Encrypt", "AES Decrypt",
                "ROT13 Encode", "ROT13 Decode",
                "HTML Entity Encode", "HTML Entity Decode"
        );

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, methods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        methodSpinner.setAdapter(adapter);

        // Toggle AES key input visibility
        methodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String method = (String) methodSpinner.getSelectedItem();
                if (method.equals("AES Encrypt") || method.equals("AES Decrypt")) {
                    aesKeyField.setVisibility(View.VISIBLE);
                } else {
                    aesKeyField.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        processButton.setOnClickListener(v -> {
            String inputText = inputField.getText().toString();
            String method = (String) methodSpinner.getSelectedItem();
            String result;

            try {
                switch (method) {
                    case "Base64 Encode":
                        result = Base64.encodeToString(inputText.getBytes(), Base64.DEFAULT);
                        break;
                    case "Base64 Decode":
                        result = new String(Base64.decode(inputText, Base64.DEFAULT));
                        break;
                    case "URL Encode":
                        result = URLEncoder.encode(inputText, "UTF-8");
                        break;
                    case "URL Decode":
                        result = URLDecoder.decode(inputText, "UTF-8");
                        break;
                    case "MD5 Hash":
                        result = hash(inputText, "MD5");
                        break;
                    case "SHA-256 Hash":
                        result = hash(inputText, "SHA-256");
                        break;
                    case "AES Encrypt":
                        String keyEnc = aesKeyField.getText().toString();
                        if (keyEnc.length() != 16 && keyEnc.length() != 24 && keyEnc.length() != 32) {
                            throw new Exception("AES key must be 16, 24, or 32 chars");
                        }
                        result = aesEncrypt(inputText, keyEnc);
                        break;
                    case "AES Decrypt":
                        String keyDec = aesKeyField.getText().toString();
                        if (keyDec.length() != 16 && keyDec.length() != 24 && keyDec.length() != 32) {
                            throw new Exception("AES key must be 16, 24, or 32 chars");
                        }
                        result = aesDecrypt(inputText, keyDec);
                        break;
                    case "ROT13 Encode":
                    case "ROT13 Decode":
                        result = rot13(inputText);
                        break;
                    case "HTML Entity Encode":
                        result = TextUtils.htmlEncode(inputText);
                        break;
                    case "HTML Entity Decode":
                        result = Html.fromHtml(inputText, Html.FROM_HTML_MODE_LEGACY).toString();
                        break;
                    default:
                        result = "Unsupported method!";
                }
            } catch (Exception e) {
                result = "Error: " + e.getMessage();
            }

            outputField.setText(result);
        });

        dialogBuilder.show();
    }

    private static String hash(String input, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    private static String aesEncrypt(String data, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return Base64.encodeToString(cipher.doFinal(data.getBytes()), Base64.DEFAULT);
    }

    private static String aesDecrypt(String data, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(Base64.decode(data, Base64.DEFAULT)));
    }

    private static String rot13(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                result.append((char) ((c - 'a' + 13) % 26 + 'a'));
            } else if (c >= 'A' && c <= 'Z') {
                result.append((char) ((c - 'A' + 13) % 26 + 'A'));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
package ru.dreadblade.czarbank.security.service;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.exception.CzarBankException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;

@Service
@RequiredArgsConstructor
public class TotpService {
    private final QrDataFactory qrDataFactory;
    private final QrGenerator qrGenerator;
    private final CodeVerifier codeVerifier;

    public byte[] generateQrCodeImage(String secretKey, String label, String issuer) {
        QrData data = qrDataFactory.newBuilder()
                .secret(secretKey)
                .label(label)
                .issuer(issuer)
                .build();

        try {
            return qrGenerator.generate(data);
        } catch (QrGenerationException exception) {
            throw new CzarBankException(ExceptionMessage.TOTP_QR_CODE_GENERATION_FAILED);
        }
    }

    public MediaType getQrCodeImageMediaType() {
        return MediaType.parseMediaType(qrGenerator.getImageMimeType());
    }

    public boolean isValidCode(String code, String secretKey) {
        return codeVerifier.isValidCode(secretKey, code);
    }
}

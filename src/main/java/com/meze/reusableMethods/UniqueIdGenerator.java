package com.meze.reusableMethods;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UniqueIdGenerator {

    public String generateUniqueId(int length) {
        String uuid = UUID.randomUUID().toString();
        String uniqueId = DigestUtils.sha256Hex(uuid);
        String lengthFormattedCode = uniqueId.substring(0,length);
        String formattedCode = lengthFormattedCode.replaceAll("(.{4})(?!$)", "$1-");

        return formattedCode.toUpperCase();
    }
}

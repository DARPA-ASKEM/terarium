package software.uncharted.terarium.hmiserver.controller.services;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import software.uncharted.terarium.hmiserver.models.documentservice.Document;

@Service
public class DocumentAssetService {
    public String getDocumentDoi(Document doc) {
        String docIdentifier = "";
        if (doc != null && doc.getIdentifier() != null && doc.getIdentifier().size() > 0) {
            for (Map<String, String> identifier : doc.getIdentifier()) {
                if (identifier.get("type").equals("doi")) {
                    docIdentifier = identifier.get("id");
                    break;
                }
            }
        }
        return docIdentifier;
    }
}

package edu.Loopi.interfaces;

import edu.Loopi.entities.Feedback;
import java.util.List;

public interface IFeedbackService {
    void addFeedback(Feedback f);               // CREATE
    List<Feedback> getFeedbacksByProduct(int idProduit); // READ (By Product)
    List<Feedback> getFeedbacksByUser(int idUser);       // READ (By User)
    void updateFeedback(Feedback f);            // UPDATE
    void deleteFeedback(int idFeedback);        // DELETE
}
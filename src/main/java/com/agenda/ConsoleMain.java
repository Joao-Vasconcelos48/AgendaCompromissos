package com.agenda;

import com.agenda.dao.ContatoDAO;
import com.agenda.model.Contato;
import com.agenda.validation.EmailValidator;
import com.agenda.validation.PhoneValidator;

import java.util.List;

public class ConsoleMain {
    public static void main(String[] args) {
        System.out.println("Inicializando banco (Database) e carregando contatos...");

        // quick validation tests
        System.out.println("\n--- Testes de validadores ---");
        String[] emails = {"alice@example.com", "invalid-email", "bob@domain", "carla.santos@empresa.br"};
        for (String e : emails) {
            System.out.println("Email: " + e + " -> " + (EmailValidator.isValid(e) ? "OK" : "INVÁLIDO"));
        }
        String[] phones = {"+55 11 99999-0001", "11999990001", "9999", "(21) 98888-0002", "abc-123"};
        for (String p : phones) {
            System.out.println("Telefone: " + p + " -> " + (PhoneValidator.isValid(p) ? "OK" : "INVÁLIDO"));
        }
        System.out.println("--- fim testes ---\n");

        ContatoDAO dao = new ContatoDAO();
        List<Contato> list = dao.findAll();
        System.out.println("Contatos encontrados: " + list.size());
        for (Contato c : list) {
            System.out.println(c.getId() + " - " + c.getNome() + " | " + c.getEmail() + " | " + c.getTelefone());
        }
        System.out.println("(Se o banco SQLite não estiver disponível nesta máquina, a execução pode falhar — use Gradle para baixar dependências.)");
    }
}

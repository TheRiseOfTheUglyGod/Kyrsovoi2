package ru.kafpin.lb7.tests;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class WarehouseAppTest extends ApplicationTest {

    private static final String VALID_USER = "manager";
    private static final String VALID_PASSWORD = "manager";

    private static final int SHORT_PAUSE = 800;
    private static final int MEDIUM_PAUSE = 1500;
    private static final int LONG_PAUSE = 2000;

    @Override
    public void start(Stage stage) throws Exception {
        new ru.kafpin.lb7.App().start(stage);
    }

    private void loginAsValidUser() throws TimeoutException {
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("#usernameField").tryQuery().isPresent());
        clickOn("#usernameField").write(VALID_USER);
        clickOn("#passwordField").write(VALID_PASSWORD);
        clickOn("Войти");
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("#contentPane").tryQuery().isPresent());
        sleep(MEDIUM_PAUSE);
    }

    // =============================================
    // ПОЗИТИВНЫЕ ТЕСТЫ
    // =============================================

    @Test
    @DisplayName("1. Добавление нового товара")
    void testAddProduct() throws TimeoutException {
        loginAsValidUser();
        clickOn("Справочники").clickOn("Товары");
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("#productsTable").tryQuery().isPresent());

        clickOn("Добавить");
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("Название:").tryQuery().isPresent());

        List<TextField> textFields = lookup(".dialog-pane .text-field").queryAll()
                .stream()
                .filter(node -> node instanceof TextField)
                .map(node -> (TextField) node)
                .collect(Collectors.toList());
        assertThat(textFields).hasSize(5);

        clickOn(textFields.get(0)).write("Тестовый товар");
        clickOn(textFields.get(1)).write("ART-TEST");
        clickOn(textFields.get(2)).write("шт");
        clickOn(textFields.get(3)).write("Тестовое описание");
        clickOn(textFields.get(4)).write("5");

        clickOn("OK");
        sleep(MEDIUM_PAUSE);

        TableView<?> tableView = lookup("#productsTable").query();
        boolean found = tableView.getItems().stream()
                .anyMatch(item -> item.toString().contains("ART-TEST"));
        assertThat(found).isTrue();

        selectTableRowBySubstring(tableView, "ART-TEST");
        clickOn("Удалить");
        sleep(SHORT_PAUSE);
    }

    @Test
    @DisplayName("2. Приход товара (сохранение накладной)")
    void testReceipt() throws TimeoutException {
        loginAsValidUser();
        clickOn("Операции").clickOn("Приход");
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("#supplierCombo").tryQuery().isPresent());
        sleep(SHORT_PAUSE);

        clickOn("#supplierCombo");
        sleep(SHORT_PAUSE);
        type(KeyCode.DOWN);
        type(KeyCode.ENTER);
        sleep(SHORT_PAUSE);

        clickOn("Добавить позицию");
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("Товар:").tryQuery().isPresent());

        List<ComboBox> comboBoxes = lookup(".dialog-pane .combo-box").queryAll()
                .stream()
                .filter(node -> node instanceof ComboBox)
                .map(node -> (ComboBox) node)
                .collect(Collectors.toList());
        assertThat(comboBoxes).hasSize(2);

        clickOn(comboBoxes.get(0));
        sleep(SHORT_PAUSE);
        clickOn("Кирпич керамический (KIR-001)");
        sleep(SHORT_PAUSE);

        List<TextField> textFields = lookup(".dialog-pane .text-field").queryAll()
                .stream()
                .filter(node -> node instanceof TextField)
                .map(node -> (TextField) node)
                .collect(Collectors.toList());
        assertThat(textFields).hasSizeGreaterThanOrEqualTo(2);
        clickOn(textFields.get(0)).write("10");
        clickOn(textFields.get(1)).write("100.00");

        clickOn(comboBoxes.get(1));
        sleep(SHORT_PAUSE);
        clickOn("Зона A, ряд 1, стеллаж R1, ячейка 10");
        sleep(SHORT_PAUSE);

        clickOn("OK");
        sleep(SHORT_PAUSE);

        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("Сохранить приход").tryQuery().isPresent());
        clickOn("Сохранить приход");
        sleep(MEDIUM_PAUSE);

        Label status = lookup("#statusLabel").query();
        assertThat(status.getText()).contains("Готов");
    }

    // =============================================
    // НЕГАТИВНЫЕ ТЕСТЫ
    // =============================================

    @Test
    @DisplayName("3. Сохранение прихода без позиций вызывает ошибку")
    void testSaveReceiptWithoutItems() throws TimeoutException {
        loginAsValidUser();
        clickOn("Операции").clickOn("Приход");
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("#supplierCombo").tryQuery().isPresent());
        sleep(SHORT_PAUSE);

        clickOn("#supplierCombo");
        sleep(SHORT_PAUSE);
        type(KeyCode.DOWN);
        type(KeyCode.ENTER);
        sleep(SHORT_PAUSE);

        // Не добавляем позиции — сразу сохраняем
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("Сохранить приход").tryQuery().isPresent());
        clickOn("Сохранить приход");

        // Ждём появления алерта
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup(".alert").tryQuery().isPresent());
        sleep(LONG_PAUSE);   // дополнительная пауза, чтобы рассмотреть сообщение
        DialogPane alert = lookup(".alert").query();
        String content = alert.getContentText();
        assertThat(content).contains("Заполните все поля");
        sleep(LONG_PAUSE);   // пауза перед закрытием, чтобы успеть прочитать
        clickOn("OK");
    }

    @Test
    @DisplayName("4. Предупреждение при удалении без выбора строки")
    void testDeleteWithoutSelection() throws TimeoutException {
        loginAsValidUser();
        clickOn("Справочники").clickOn("Товары");
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("#productsTable").tryQuery().isPresent());

        clickOn("Удалить");

        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup(".alert").tryQuery().isPresent());
        sleep(LONG_PAUSE);   // пауза, чтобы увидеть предупреждение
        DialogPane alert = lookup(".alert").query();
        assertThat(alert.getContentText()).contains("Выберите товар");
        sleep(LONG_PAUSE);   // пауза перед закрытием
        clickOn("OK");
    }

    private void selectTableRowBySubstring(TableView<?> tableView, String substring) {
        int idx = -1;
        int i = 0;
        for (Object item : tableView.getItems()) {
            if (item.toString().contains(substring)) {
                idx = i;
                break;
            }
            i++;
        }
        if (idx == -1) throw new IllegalStateException("Строка не найдена: " + substring);
        final int target = idx;
        interact(() -> tableView.scrollTo(target));
        sleep(SHORT_PAUSE);
        clickOn(substring);
    }
}
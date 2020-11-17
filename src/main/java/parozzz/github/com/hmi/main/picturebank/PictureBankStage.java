package parozzz.github.com.hmi.main.picturebank;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.converter.DefaultStringConverter;
import parozzz.github.com.hmi.page.HMIStage;
import parozzz.github.com.hmi.util.ContextMenuBuilder;
import parozzz.github.com.hmi.util.EnumStringConverter;
import parozzz.github.com.hmi.util.FXImageUtil;
import parozzz.github.com.hmi.util.FXUtil;
import parozzz.github.com.logger.MainLogger;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.function.Consumer;

public class PictureBankStage extends HMIStage<BorderPane>
{
    public enum WhiteToleranceEnum
    {
        NONE(255),
        LOW(250),
        MEDIUM(240),
        HIGH(225);

        private final int tolerance;

        WhiteToleranceEnum(int tolerance)
        {
            this.tolerance = tolerance;
        }
    }

    @FXML private ListView<String> fileListView;
    @FXML private ImageView previewImageView;
    @FXML private Button addNewButton;

    @FXML private Label whiteToleranceLabel;
    @FXML private ChoiceBox<WhiteToleranceEnum> whiteToleranceChoiceBox;

    @FXML private Button selectImageButton;

    private final String directoryPath;
    private Consumer<File> selectFileConsumer;

    public PictureBankStage() throws IOException
    {
        super("Picture Bank", "pictureBankPane.fxml", BorderPane.class);

        directoryPath = System.getProperty("user.dir") + "\\picture_bank";
    }

    @Override
    public void setup()
    {
        super.setup();

        super.serializableDataSet.addEnum("WhiteTolerance", whiteToleranceChoiceBox.valueProperty(), WhiteToleranceEnum.class);

        super.getStageSetter().setAlwaysOnTop(true)
                .setResizable(true)
                .setOnWindowCloseRequest(windowEvent -> this.revertButtonsToDefaultVisibility());

        fileListView.setEditable(true);
        fileListView.setCellFactory(listView ->
        {
            var listCell = new TextFieldListCell<String>()
            {
                @Override
                public void updateItem(String string, boolean empty)
                {
                    super.updateItem(string, empty);
                    setText(Objects.requireNonNullElse(string, ""));
                }
            };

            listCell.setContextMenu(ContextMenuBuilder.builder()
                    .simple("Remove White Background", this::removeWhiteBackgroundFromSelectedImage)
                    .simple("Clone", this::cloneSelectedImage)
                    .simple("Delete", this::removeSelectedImage)
                    .getContextMenu()
            );

            listCell.setConverter(new DefaultStringConverter());
            listCell.setEditable(true);
            listCell.textProperty().addListener((observableValue, oldValue, newValue) ->
            {
                if(oldValue == null || newValue == null)
                {
                    return;
                }

                var file = new File(directoryPath, oldValue);
                if(file.exists())
                {
                    file.renameTo(new File(directoryPath, newValue));
                }
            });

            return listCell;
        });

        fileListView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) ->
        {
            if(newValue != null)
            {
                var file = new File(directoryPath, newValue);
                if(file.exists())
                {
                    previewImageView.setImage(new Image(file.toURI().toString()));
                }
            }
        });

        addNewButton.setOnAction(actionEvent ->
        {
            var fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(FXUtil.IMAGE_EXTENSION_FILTER);

            var fileList = fileChooser.showOpenMultipleDialog(this.getStageSetter().get()); //Allow to select multiple files...
            if(fileList == null)
            {
                return;
            }

            fileList.stream().filter(Objects::nonNull).forEach(file ->
            {
                var copyFile = new File(directoryPath, file.getName());
                if(!copyFile.exists())
                {
                    try
                    {
                        Files.copy(file.toPath(), copyFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                        fileListView.getItems().add(copyFile.getName());
                    }
                    catch(IOException exception)
                    {
                        MainLogger.getInstance().error("Error while importing picture into picture bank", exception, this);
                    }
                }
            });
        });

        whiteToleranceChoiceBox.setConverter(new EnumStringConverter<>(WhiteToleranceEnum.class).setCapitalize());
        whiteToleranceChoiceBox.getItems().addAll(WhiteToleranceEnum.values());
        whiteToleranceChoiceBox.setValue(WhiteToleranceEnum.LOW);

        selectImageButton.setOnAction(actionEvent ->
        {
            var selectedItem = fileListView.getSelectionModel().getSelectedItem();
            if(selectFileConsumer == null)
            {//Something is wrong. In this case, better revert!
                this.revertButtonsToDefaultVisibility();
                return;
            }

            if(selectedItem == null)
            {//In this case, anything has been selected and wait until it is!
                return;
            }

            var file = new File(directoryPath, selectedItem);
            if(file.exists())
            {
                selectFileConsumer.accept(file);
                this.getStageSetter().close();
            }

            this.revertButtonsToDefaultVisibility();
        });

        selectImageButton.setVisible(false);

        var directory = new File(directoryPath);
        if(!directory.exists())
        {
            directory.mkdirs();
        }else
        {
            var fileList = directory.listFiles();
            if(fileList != null)
            {
                for(var file : fileList)
                {
                    fileListView.getItems().add(file.getName());
                }
            }
        }

    }

    @Override
    public void setupComplete()
    {
        super.setupComplete();
    }

    public URI getImageURI(File file)
    {
        if(file != null && file.exists() && directoryPath.equals(file.getParent()))
        {
            return file.toURI();
        }

        return null;
    }

    public URI getImageURI(String fileName)
    {
        return fileName == null
               ? null
               : getImageURI(new File(directoryPath, fileName));
    }

    public void startImageSelection(Consumer<File> consumer)
    {
        addNewButton.setVisible(false);
        whiteToleranceLabel.setVisible(false);
        whiteToleranceChoiceBox.setVisible(false);
        selectImageButton.setVisible(true);

        this.selectFileConsumer = consumer;

        this.getStageSetter().get().show();
    }

    public void revertButtonsToDefaultVisibility()
    {
        addNewButton.setVisible(true);
        whiteToleranceLabel.setVisible(true);
        whiteToleranceChoiceBox.setVisible(true);
        selectImageButton.setVisible(false);

        selectFileConsumer = null;
    }

    private void removeWhiteBackgroundFromSelectedImage()
    {
        var file = this.getSelectedFile();
        if(file == null)
        {
            return;
        }

        try
        {
            var convertedImage = FXImageUtil.removeWhiteBackground(new Image(file.toURI().toString()),
                    whiteToleranceChoiceBox.getValue().tolerance);

            var bufferedImage = SwingFXUtils.fromFXImage(convertedImage, null);
            ImageIO.write(bufferedImage, "png", file);

            previewImageView.setImage(convertedImage);
        }
        catch(Exception exception)
        {
            MainLogger.getInstance().error("Error while removing white background", exception, this);
        }
    }

    private void cloneSelectedImage()
    {
        var file = this.getSelectedFile();
        if(file == null)
        {
            return;
        }

        var fileName = file.getName();
        for(int x = 1; x < 1024; x++)
        {
            var lastDotIndex = fileName.lastIndexOf('.');

            var extension = fileName.substring(lastDotIndex);
            var fileNameWithoutExtension = fileName.substring(0, lastDotIndex);

            var clonedFile = new File(directoryPath, fileNameWithoutExtension + "_" + x + extension);
            if(!clonedFile.exists())
            {
                try
                {
                    Files.copy(file.toPath(), clonedFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                    fileListView.getItems().add(clonedFile.getName());
                }
                catch(IOException exception)
                {
                    MainLogger.getInstance().error("Error while cloning picture bank image", exception, this);
                }
                break;
            }
        }

    }

    private void removeSelectedImage()
    {
        var selectedItem = fileListView.getSelectionModel().getSelectedItem();
        if(selectedItem != null)
        {
            new File(directoryPath, selectedItem).delete();
            fileListView.getItems().remove(selectedItem);

            previewImageView.setImage(null);
        }
    }

    private File getSelectedFile()
    {
        var selectedItem = fileListView.getSelectionModel().getSelectedItem();
        if(selectedItem != null)
        {
            var file = new File(directoryPath, selectedItem);
            if(file.exists())
            {
                return file;
            }
        }

        return null;
    }
}

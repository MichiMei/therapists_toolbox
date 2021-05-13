package ContentClasses;

import javax.swing.*;
import java.io.Serializable;

public interface Page extends Serializable {
    JPanel createPanel();
}

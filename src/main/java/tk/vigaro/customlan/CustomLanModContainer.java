package tk.vigaro.customlan;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

import java.util.Arrays;

public class CustomLanModContainer extends DummyModContainer {
    public static final String MODID = "customlan";
    public static final String VERSION = "1.0";
    public static int lanPort;

    public CustomLanModContainer() {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = MODID;
        meta.name = "CustomLan";
        meta.description = "";
        meta.version = "1.10.2-" + VERSION;
        meta.authorList = Arrays.asList("Vigaro");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

    public static int getLanPort() {
        return lanPort;
    }

}

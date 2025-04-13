// Абстрактні продукти
public interface IButton { void Render(); }
public interface IMenu { void Display(); }

// Конкретні продукти для Windows
public class WindowsButton : IButton
{
    public void Render() => Console.WriteLine("Рендер кнопки Windows");
}
public class WindowsMenu : IMenu
{
    public void Display() => Console.WriteLine("Відображення меню Windows");
}

// Конкретні продукти для macOS
public class MacOSButton : IButton
{
    public void Render() => Console.WriteLine("Рендер кнопки macOS");
}
public class MacOSMenu : IMenu
{
    public void Display() => Console.WriteLine("Відображення меню macOS");
}

// Абстрактна фабрика
public interface IGUIFactory
{
    IButton CreateButton();
    IMenu CreateMenu();
}

// Конкретні фабрики
public class WindowsFactory : IGUIFactory
{
    public IButton CreateButton() => new WindowsButton();
    public IMenu CreateMenu() => new WindowsMenu();
}
public class MacOSFactory : IGUIFactory
{
    public IButton CreateButton() => new MacOSButton();
    public IMenu CreateMenu() => new MacOSMenu();
}

// Клієнтський код
public class Client
{
    private readonly IButton _button;
    private readonly IMenu _menu;
    public Client(IGUIFactory factory)
    {
        _button = factory.CreateButton();
        _menu = factory.CreateMenu();
    }
    public void RenderUI()
    {
        _button.Render();
        _menu.Display();
    }
}

// Використання
class Program
{
    static void Main()
    {
        IGUIFactory factory = new WindowsFactory(); // або MacOSFactory
        Client client = new Client(factory);
        client.RenderUI();
    }
}
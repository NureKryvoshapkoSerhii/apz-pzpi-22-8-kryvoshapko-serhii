#include <iostream>
#include <string>
class View {
public:
    virtual void Render() = 0;
    virtual ~View() {}
};
class Button : public View {
public:
    virtual void Render() override = 0;
};
class Menu : public View {
public:
    virtual void Render() override = 0;
};
class WindowsButton : public Button {
public:
    void Render() override { std::cout << "Windows button rendered\n"; }
};
class WindowsMenu : public Menu {
public:
    void Render() override { std::cout << "Windows menu rendered\n"; }
};
class MacOSButton : public Button {
public:
    void Render() override { std::cout << "macOS button rendered\n"; }
};
class MacOSMenu : public Menu {
public:
    void Render() override { std::cout << "macOS menu rendered\n"; }
};
class ViewFactory {
public:
    virtual Button* CreateButton() = 0;
    virtual Menu* CreateMenu() = 0;
    virtual ~ViewFactory() {}
};
class WindowsViewFactory : public ViewFactory {
public:
    Button* CreateButton() override { return new WindowsButton(); }
    Menu* CreateMenu() override { return new WindowsMenu(); }
};
class MacOSViewFactory : public ViewFactory {
public:
    Button* CreateButton() override { return new MacOSButton(); }
    Menu* CreateMenu() override { return new MacOSMenu(); }
};
class BrowserUI {
    Button* button;
    Menu* menu;
public:
    BrowserUI(ViewFactory* factory) {
        button = factory->CreateButton();
        menu = factory->CreateMenu();
    }
    void Render() {
        button->Render();
        menu->Render();
    }
    ~BrowserUI() {
        delete button;
        delete menu;
    }
};
int main() {
    ViewFactory* factory = nullptr;
    #ifdef _WIN32
        factory = new WindowsViewFactory();
    #else
        factory = new MacOSViewFactory();
    #endif
    BrowserUI ui(factory);
    ui.Render();
    delete factory;
    return 0;
}
package controller.task;

import controller.ServiceLocator;

public class GameController implements IGameController {

    private ServiceLocator serviceLocator;

    public GameController(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }
    @Override
    public ServiceLocator getServiceLocator() {
        return null;
    }
}

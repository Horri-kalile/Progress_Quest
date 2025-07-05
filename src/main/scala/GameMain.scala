import view.{GameUi, PlayerGenerationUi}
import controllers.GameController

object GameMain extends App:
  PlayerGenerationUi.launch(player =>
    GameController.startGame(player)
    GameUi.playerOpt = Some(player)
    GameUi.open()
  )


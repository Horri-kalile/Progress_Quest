import view.{GameUi, PlayerGenerationUi}
import controllers.GameController

/** Main entry point for the Progress Quest application.
  *
  * This object launches the character creation interface and then starts the main game when a player is created.
  */
object GameMain extends App:
  /** Application startup sequence:
    *   1. Launch player creation UI
    *   2. Start game controller with created player
    *   3. Set player in game UI
    *   4. Open main game window
    */
  PlayerGenerationUi.launch(player =>

    // Start the game logic with the created player
    GameController.startGame(player)

    // Set the player reference for UI updates
    GameUi.playerOpt = Some(player)

    // Open the main game interface
    GameUi.open(PlayerGenerationUi.mainStage)
  )

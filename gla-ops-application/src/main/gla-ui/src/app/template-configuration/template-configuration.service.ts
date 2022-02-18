import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TemplateConfigurationService {

  constructor() { }

  getExistingComment(command, block){
    switch (command.name) {
      case "UPDATE_DISPLAY_NAME":
        return block.blockDisplayName

      case "UPDATE_INFO_MESSAGE":
        return block.infoMessage;

      default:
        return null;
    }
  }

  setUserInputComment(command, blockData, userInput){
    switch (command.name) {
      case "UPDATE_DISPLAY_NAME":
        blockData['blockNewName'] = userInput
        break

      case "UPDATE_INFO_MESSAGE":
        blockData['infoMessage'] = userInput
        break

      case "REMOVE_QUESTION":
        blockData['infoMessage'] = userInput
        break

      default:
        break
    }

  }

}

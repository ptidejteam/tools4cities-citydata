from metamenth.subsystems.hvac_system import HvacSystem
from pickle import NONE

class BuildingControlSystem:
    def __init__(self, name: str):
        self._hvac_system = None
        self._name = NONE
        self.setName(name)
        
    def getName(self) -> str:
        return self._name
    
    def setName(self, value:str):
        self._name = value
        
    def getHvacSystem(self) -> HvacSystem:
        return self._hvac_system
    
    def setHvacSystem(self, value: HvacSystem):
        self._hvac_system = value
        
    def __str__(self):
        return(
            f"BuildingControlSystem("
            f"Name: {self.getName()}, "
            f"Hvac System: {self.getHvacSystem()},"            
            )
        
    def toString(self) -> str:
        return self.__str__()
        
    class Java:    
        implements = ["ca.concordia.ngci.tools4cities.metamenth.interfaces.subsystems.IBuildingControlSystem"]        
    
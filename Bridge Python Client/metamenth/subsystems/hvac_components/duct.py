import uuid

class Duct:
    def __init__(self, name: str, duct_type: str):
        self._UID = str(uuid.uuid4())
        self._name = None
        self._duct_type = None
        
        self.setName(name)
        self.steDuctType(duct_type)
        
    def getUID(self) -> str:
        return self._UID
    
    def getName(self) -> str:
        return self._name
    
    def setName(self, value:str):
        self._name = value
        
    def getDuctType(self) -> str:
        return self._duct_type
    
    def setDuctType(self, value:str):
        self._duct_type = value
        
    def __str__(self):
        return(
            f"Duct("
            f"UID: {self.getUID()}, "
            f"Name: {self.getName()}, "
            f"DuctType: {self.getDuctType()})"
            
            )
        
    def toString(self) -> str:
        return self.__str__()
    
    class Java:
        implements = {"ca.concordia.ngci.tools4cities.metamenth.interfaces.subsystems.hvac_components.IDuct"}
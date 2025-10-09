from metamenth.subsystems.hvac_components.duct import Duct
from py4j.java_gateway import JavaGateway

import uuid
 
class VentilationSystem:
    def __init__(self, ventilation_type: str, principal_duct: Duct):
        gateway = JavaGateway
        
        self._UID = str(uuid.uuid4())
        self._ventilation_type = None
        self._principal_duct = None
        self._component = gateway.jvm.java.util.ArrayList()
        
    def getUID(self) -> str:
        return self._UID
    
    def getVentilationType(self) -> str:
        return self._ventilation_type
    
    def setVentilationType(self, value:str):
        self._ventilation_type = value
        
    def getPrincipalDuct(self) -> str:
        return self._principal_duct
    
    def setPrincipalDuct(self, value:str):
        self._principal_duct = value
        
    def __str__(self):
        return(
            f"VentilationSystem("
            f"UID: {self.getUID()}, "
            f"Ventilation Type: {self.getVentilationType()}, "
            f"Principal Duct: {self.getPrincipalDuct()})"
            
            )
        
    def toString(self) -> str:
        return self.__str__()
    
    class Java:
        implements = {"ca.concordia.ngci.tools4cities.metamenth.interfaces.subsystems.IVentilationSystem"}
        
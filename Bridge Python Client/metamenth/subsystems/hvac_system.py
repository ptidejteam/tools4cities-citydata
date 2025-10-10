import uuid
from py4j.java_gateway import JavaGateway
from metamenth.subsystems.ventilation_system import VentilationSystem


class HvacSystem:
    def __init__(self):
        gateway = JavaGateway()
        
        self._UID = str(uuid.uuid4())
        self._ventilation_system = gateway.jvm.java.util.ArrayList()
        
    def getUID(self) -> str:
        return self._UID
    
    def getVentilationSystem(self) -> [VentilationSystem]:
        return self._ventilation_system
    
    def addVentilationSystem(self, value: VentilationSystem):
        self._ventilation_system.append(value)
        
    def __str__(self):
        return(
            f"HvacSystem("
            f"UID: {self.getUID()}, "
            f"Ventilation System: {self.getVentilationSystem()},"            
            )
        
    def toString(self) -> str:
        return self.__str__()
    
    class Java:
        implements = {"ca.concordia.ngci.tools4cities.metamenth.interfaces.subsystems.IHvacSystem"}